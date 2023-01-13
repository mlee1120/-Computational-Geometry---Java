/*
 * This file illustrates TrapezoidalMap.java from assignment 3.
 */

import java.io.*;
import java.util.*;

/**
 * This program first takes its first system argument as the filename of the input file
 * and reads the segments' information from the file. After that. it performs incremental
 * construction algorithm to compute the trapezoidal map of those segments. And then, it
 * builds an adjacency matrix as a representation of the trapezoidal map and prints it out
 * at the standard output. At last, it is able to accept command line input of a query point
 * (x-value y-value) and display the resulting path through the rooted directed acyclic graph
 * (the tree) as a string
 *
 * @author Michael Lee, ml3406@rit.edu
 */
public class TrapezoidalMap {
    /**
     * number of line segments
     */
    public int n;

    /**
     * the lower left and upper right coordinates of a bounding box around all segments
     */
    public int[] boundingBox;

    /**
     * a list of all segments
     */
    public List<int[]> segments;

    /**
     * the trapezoidal map represented as a tree (rooted directed acyclic graph)
     */
    public MyTree tree;

    /**
     * a set to store the indices of all trapezoids
     */
    public Set<Integer> trapezoids;

    /**
     * the trapezoidal map represented as an adjacency matrix
     */
    public List<List<Integer>> adjacencyMatrix;

    /**
     * The constructor initializes some important fields.
     */
    public TrapezoidalMap() {
        boundingBox = new int[4];
        segments = new ArrayList<>();
        tree = new MyTree();
        trapezoids = new HashSet<>();
        adjacencyMatrix = new ArrayList<>();
    }

    /**
     * This method reads the input segments with a bounding box from
     * a text file and store them to corresponding data structures.
     *
     * @param filename - the input filename
     */
    public void input(String filename) {
        try (BufferedReader input = new BufferedReader(new FileReader(filename))) {
            // number of line segments
            n = Integer.parseInt(input.readLine());
            String segment = input.readLine();
            String[] splitSegment = segment.split(" ");
            boundingBox[0] = Integer.parseInt(splitSegment[0]);
            boundingBox[1] = Integer.parseInt(splitSegment[1]);
            boundingBox[2] = Integer.parseInt(splitSegment[2]);
            boundingBox[3] = Integer.parseInt(splitSegment[3]);
            int x1, y1, x2, y2;

            for (int i = 0; i < n; i++) {
                segment = input.readLine();
                splitSegment = segment.split(" ");
                x1 = Integer.parseInt(splitSegment[0]);
                y1 = Integer.parseInt(splitSegment[1]);
                x2 = Integer.parseInt(splitSegment[2]);
                y2 = Integer.parseInt(splitSegment[3]);
                segments.add(new int[]{x1, y1, x2, y2});
            }
        } catch (IOException e) {
            System.out.println("File not found or input error!");
        }
    }

    /**
     * This method performs incremental construction algorithm by adding one
     * segment to the trapezoidal map at a time. The map is updated while a
     * segment is added (fix all existing trapezoids that are intersected
     * with the newly added segments).
     */
    public void incremental() {
        // a list to store all trapezoids to be fixed when adding a new segment
        List<Node> trapezoidsToFix = new ArrayList<>();

        // the relative position of the left endpoint and its parent affects how to tree is built
        boolean aboveOrBelow = true;

        // a helper set to check if a trapezoid to be fixed is already discovered
        Set<Integer> added = new HashSet<>();

        // add one line segment at a time
        for (int i = 0; i < n; i++) {
            trapezoidsToFix.clear();
            added.clear();
            // if the left endpoint and the right endpoint are intersected with other endpoints or not
            boolean endpointLeftOverlap = false, endpointRightOverlap = false;

            // first segment must be case 2 (9-1 lecture slides)
            if (i == 0) {
                tree.root = new Node("P", i + 1);
                tree.root.left = new Node("T", 1);
                tree.root.right = new Node("Q", i + 1);
                tree.root.right.left = new Node("S", i + 1);
                tree.root.right.left.left = new Node("T", 2);
                tree.root.right.left.right = new Node("T", 3);
                tree.root.right.right = new Node("T", 4);
                trapezoids.add(1);
                trapezoids.add(2);
                trapezoids.add(3);
                trapezoids.add(4);
            }

            // other segments
            else {
                int x1 = segments.get(i)[0];
                int y1 = segments.get(i)[1];
                int x2 = segments.get(i)[2];
                int y2 = segments.get(i)[3];

                // the segment's slope and y-intercept (for checking relative position of segments)
                double a = ((double) (y2 - y1)) / (x2 - x1);
                double b = y1 - a * x1;

                // walk along the segment to discover all intersected trapezoids
                for (int j = x1; j <= x2; j++) {
                    Node current = tree.root;
                    boolean toFix = true;
                    boolean carryOn = true;
                    do {
                        switch (current.type) {
                            // trapezoid (leaf node)
                            case "T":
                                carryOn = false;
                                break;

                            // left endpoint (x-node)
                            case "P":
                                if (j > segments.get(current.index - 1)[0]) {
                                    // the right node of a p (left endpoint) won't be a leaf
                                    current = current.right;
                                } else if (j < segments.get(current.index - 1)[0]) {
                                    // if the left node is a leaf
                                    if (current.left.type.equals("T") && j == x1) {
                                        aboveOrBelow = a * j + b > segments.get(current.index - 1)[1];
                                    }
                                    current = current.left;
                                }

                                // handle intersection of two left endpoints
                                else {
                                    // left endpoint
                                    if (j == x1) {
                                        endpointLeftOverlap = true;
                                        current = current.right;
                                    } else {
                                        toFix = false;
                                        carryOn = false;
                                    }
                                }
                                break;

                            // right endpoint (x-node)
                            case "Q":
                                if (j > segments.get(current.index - 1)[2]) {
                                    // if the right node is a leaf
                                    if (current.right.type.equals("T") && j == x1) {
                                        aboveOrBelow = a * j + b > segments.get(current.index - 1)[1];
                                    }
                                    current = current.right;
                                } else if (j < segments.get(current.index - 1)[2]) {
                                    // the left node of a q (right endpoint) won't be a leaf
                                    current = current.left;
                                }

                                // handle intersection of two right endpoints
                                else {
                                    // right endpoint
                                    if (j == x2) {
                                        endpointRightOverlap = true;
                                        current = current.left;
                                    } else {
                                        toFix = false;
                                        carryOn = false;
                                    }
                                }
                                break;

                            // segment (y-node)
                            case "S":
                                if (j == x1 && x1 == segments.get(current.index - 1)[0] && y1 == segments.get(current.index - 1)[1]) {
                                    // above
                                    if (y2 > segments.get(current.index - 1)[3]) {
                                        aboveOrBelow = true;
                                        current = current.left;
                                    }
                                    // below
                                    else {
                                        aboveOrBelow = false;
                                        current = current.right;
                                    }
                                } else if (j == x2 && x2 == segments.get(current.index - 1)[2] && y2 == segments.get(current.index - 1)[3]) {
                                    // above
                                    if (y1 > segments.get(current.index - 1)[1]) {
                                        current = current.left;
                                    }
                                    // below
                                    else {
                                        current = current.right;
                                    }
                                } else {
                                    int xTemp = segments.get(current.index - 1)[0];
                                    int yTemp = segments.get(current.index - 1)[1];
                                    double dx1 = segments.get(current.index - 1)[2] - xTemp;
                                    double dx2 = j - xTemp;
                                    double dy1 = segments.get(current.index - 1)[3] - yTemp;
                                    double dy2 = ((double) (y1 * (x2 - j) + y2 * (j - x1))) / (x2 - x1) - yTemp;
                                    // above
                                    if (dy1 / dx1 < dy2 / dx2) {
                                        if (j == x1) aboveOrBelow = true;
                                        current = current.left;
                                    }
                                    // below
                                    else {
                                        if (j == x1) aboveOrBelow = false;
                                        current = current.right;
                                    }
                                }
                                break;
                        }
                    } while (carryOn);

                    // add trapezoids to be fixed (no duplicate)
                    if (toFix && !added.contains(current.index)) {
                        trapezoidsToFix.add(current);
                        added.add(current.index);
                    }
                }

                // fix all intersected trapezoids
                Node leftTemp = null, rightTemp = null;
                for (int k = 0; k < trapezoidsToFix.size(); k++) {
                    // only one trapezoid to fix
                    if (trapezoidsToFix.size() == 1) {
                        // case 2 (9-1 lecture slides)
                        if (!endpointLeftOverlap && !endpointRightOverlap) {
                            trapezoidsToFix.get(0).left = new Node("T", trapezoidsToFix.get(0).index);
                            trapezoidsToFix.get(0).type = "P";
                            trapezoidsToFix.get(0).index = i + 1;
                            trapezoidsToFix.get(0).right = new Node("Q", i + 1);
                            trapezoidsToFix.get(0).right.left = new Node("S", i + 1);
                            trapezoidsToFix.get(0).right.left.left = new Node("T", trapezoids.size() + 1);
                            trapezoidsToFix.get(0).right.left.right = new Node("T", trapezoids.size() + 2);
                            trapezoidsToFix.get(0).right.right = new Node("T", trapezoids.size() + 3);
                            trapezoids.add(trapezoids.size() + 1);
                            trapezoids.add(trapezoids.size() + 1);
                            trapezoids.add(trapezoids.size() + 1);
                        }

                        // case 1 (left endpoint)
                        else if (!endpointLeftOverlap) {
                            trapezoidsToFix.get(0).left = new Node("T", trapezoidsToFix.get(0).index);
                            trapezoidsToFix.get(0).type = "P";
                            trapezoidsToFix.get(0).index = i + 1;
                            trapezoidsToFix.get(0).right = new Node("S", i + 1);
                            trapezoidsToFix.get(0).right.left = new Node("T", trapezoids.size() + 1);
                            trapezoidsToFix.get(0).right.right = new Node("T", trapezoids.size() + 2);
                            trapezoids.add(trapezoids.size() + 1);
                            trapezoids.add(trapezoids.size() + 1);
                        }

                        // case 1 (right endpoint)
                        else if (!endpointRightOverlap) {
                            trapezoidsToFix.get(0).left = new Node("S", i + 1);
                            trapezoidsToFix.get(0).left.left = new Node("T", trapezoidsToFix.get(0).index);
                            trapezoidsToFix.get(0).left.right = new Node("T", trapezoids.size() + 1);
                            trapezoidsToFix.get(0).right = new Node("T", trapezoids.size() + 2);
                            trapezoidsToFix.get(0).type = "Q";
                            trapezoidsToFix.get(0).index = i + 1;
                            trapezoids.add(trapezoids.size() + 1);
                            trapezoids.add(trapezoids.size() + 1);
                        }
                    }

                    // two or more trapezoids to fix
                    else {
                        // first trapezoid (the left endpoint might be intersected with another endpoint)
                        if (k == 0) {
                            // case 1 (no intersection)
                            trapezoidsToFix.get(k).left = new Node("T", trapezoidsToFix.get(k).index);
                            if (!endpointLeftOverlap) {
                                trapezoidsToFix.get(k).type = "P";
                                trapezoidsToFix.get(k).index = i + 1;
                                trapezoidsToFix.get(k).right = new Node("S", i + 1);
                                trapezoidsToFix.get(k).right.left = new Node("T", trapezoids.size() + 1);
                                trapezoidsToFix.get(k).right.right = new Node("T", trapezoids.size() + 2);
                                trapezoids.add(trapezoids.size() + 1);
                            }
                            // case 3 (intersection)
                            else {
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                trapezoidsToFix.get(k).right = new Node("T", trapezoids.size() + 1);
                            }
                            trapezoids.add(trapezoids.size() + 1);
                            leftTemp = trapezoidsToFix.get(k).right.left;
                            rightTemp = trapezoidsToFix.get(k).right.right;
                        }

                        // last trapezoid (the right endpoint might be intersected with another endpoint)
                        else if (k == trapezoidsToFix.size() - 1) {
                            // case 1 (no intersection)
                            if (!endpointRightOverlap) {
                                trapezoidsToFix.get(k).left = new Node("S", i + 1);
                                if (aboveOrBelow) {
                                    trapezoidsToFix.get(k).left.left = leftTemp;
                                    trapezoidsToFix.get(k).left.right = new Node("T", trapezoidsToFix.get(k).index);
                                } else {
                                    trapezoidsToFix.get(k).left.left = new Node("T", trapezoidsToFix.get(k).index);
                                    trapezoidsToFix.get(k).left.right = rightTemp;
                                }
                                trapezoidsToFix.get(k).right = new Node("T", trapezoids.size() + 1);
                                trapezoidsToFix.get(k).type = "Q";
                                trapezoidsToFix.get(k).index = i + 1;
                                trapezoids.add(trapezoids.size() + 1);
                            }
                            // case 3 (intersection)
                            else {
                                if (aboveOrBelow) {
                                    trapezoidsToFix.get(k).left = leftTemp;
                                    trapezoidsToFix.get(k).right = new Node("T", trapezoidsToFix.get(k).index);
                                } else {
                                    trapezoidsToFix.get(k).left = new Node("T", trapezoidsToFix.get(k).index);
                                    trapezoidsToFix.get(k).right = rightTemp;
                                }
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                leftTemp = trapezoidsToFix.get(k).right.left;
                                rightTemp = trapezoidsToFix.get(k).right.right;
                            }
                        }

                        // trapezoids in between
                        else {
                            // case 3 (the segment is above and the previous one is the left endpoint's trapezoid)
                            if (k == 1 && aboveOrBelow && !endpointLeftOverlap) {
                                trapezoidsToFix.get(k).left = leftTemp;
                                trapezoidsToFix.get(k).right = new Node("T", trapezoidsToFix.get(k).index);
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                leftTemp = trapezoidsToFix.get(k).right.left;
                                rightTemp = trapezoidsToFix.get(k).right.right;
                            }

                            // case 3 (the segment is above and the previous one is not the left endpoint's trapezoid)
                            else if (k == 1 && aboveOrBelow) {
                                trapezoidsToFix.get(k).left = new Node("T", trapezoidsToFix.get(k).index);
                                trapezoidsToFix.get(k).right = rightTemp;
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                leftTemp = trapezoidsToFix.get(k).left;
                                rightTemp = trapezoidsToFix.get(k).right;
                            }

                            // case 3 (the segment is below and the previous one is the left endpoint's trapezoid)
                            else if (k == 1 && !endpointLeftOverlap) {
                                trapezoidsToFix.get(k).left = new Node("T", trapezoidsToFix.get(k).index);
                                trapezoidsToFix.get(k).right = rightTemp;
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                leftTemp = trapezoidsToFix.get(k).left;
                                rightTemp = trapezoidsToFix.get(k).right;
                            }
                            // case 3 (the segment is below and the previous one is not the left endpoint's trapezoid)
                            else if (k == 1) {
                                trapezoidsToFix.get(k).left = leftTemp;
                                trapezoidsToFix.get(k).right = new Node("T", trapezoidsToFix.get(k).index);
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                leftTemp = trapezoidsToFix.get(k).left;
                                rightTemp = trapezoidsToFix.get(k).right;
                            }
                            // case 3 (the previous one is also a trapezoid in between)
                            else {
                                if (aboveOrBelow) {
                                    trapezoidsToFix.get(k).right = rightTemp;
                                    trapezoidsToFix.get(k).left = new Node("T", trapezoidsToFix.get(k).right.index);
                                } else {
                                    trapezoidsToFix.get(k).left = leftTemp;
                                    trapezoidsToFix.get(k).right = new Node("T", trapezoidsToFix.get(k).right.index);
                                }
                                trapezoidsToFix.get(k).type = "S";
                                trapezoidsToFix.get(k).index = i + 1;
                                leftTemp = trapezoidsToFix.get(k).left;
                                rightTemp = trapezoidsToFix.get(k).right;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method builds the adjacency matrix that represents the trapezoidal map.
     */
    public void buildMatrix() {
        // number of left endpoints + number of right endpoints + number of segments + number of trapezoids + sum row
        int number = n + n + n + trapezoids.size() + 1;

        // blank matrix
        for (int i = 0; i < number; i++) {
            adjacencyMatrix.add(new ArrayList<>());
            for (int j = 0; j < number; j++) {
                adjacencyMatrix.get(i).add(0);
            }
        }

        // fill in matrix by traversing the tree
        traverse(tree.root);

        // calculate sum
        int sumRow = 0, sumColumn = 0;
        for (int i = 0; i < adjacencyMatrix.size() - 1; i++) {
            for (int j = 0; j < adjacencyMatrix.size() - 1; j++) {
                if (adjacencyMatrix.get(i).get(j) == 1) sumRow++;
                if (adjacencyMatrix.get(j).get(i) == 1) sumColumn++;
            }
            adjacencyMatrix.get(i).set(adjacencyMatrix.size() - 1, sumRow);
            adjacencyMatrix.get(adjacencyMatrix.size() - 1).set(i, sumColumn);
            sumRow = 0;
            sumColumn = 0;
        }
    }

    /**
     * This is a helper function of buildMatrix() that traverses the trapezoidal map
     * (in form of rooted directed acyclic graph) recursively to build the adjacency
     * matrix.
     *
     * @param current - the current node
     */
    public void traverse(Node current) {
        if (!current.type.equals("T")) {
            int index1, index2, index3;
            index1 = switch (current.left.type) {
                case "P" -> current.left.index - 1;
                case "Q" -> n + current.left.index - 1;
                case "S" -> 2 * n + current.left.index - 1;
                default -> 3 * n + current.left.index - 1;
            };

            index2 = switch (current.right.type) {
                case "P" -> current.right.index - 1;
                case "Q" -> n + current.right.index - 1;
                case "S" -> 2 * n + current.right.index - 1;
                default -> 3 * n + current.right.index - 1;
            };

            index3 = switch (current.type) {
                case "P" -> current.index - 1;
                case "Q" -> n + current.index - 1;
                case "S" -> 2 * n + current.index - 1;
                default -> 3 * n + current.index - 1;
            };

            adjacencyMatrix.get(index1).set(index3, 1);
            adjacencyMatrix.get(index2).set(index3, 1);

            traverse(current.left);
            traverse(current.right);
        }
    }

    /**
     * This method exports a text file containing the adjacency matrix of the trapezoidal map.
     */
    public void output() {
        try (BufferedWriter output = new BufferedWriter(new FileWriter("Adjacency Matrix.txt"))) {
            output.write("    ");
            for (int i = 0; i < n; i++) {
                output.write("P" + (i + 1) + " ");
            }
            for (int i = 0; i < n; i++) {
                output.write("Q" + (i + 1) + " ");
            }
            for (int i = 0; i < n; i++) {
                output.write("S" + (i + 1) + " ");
            }
            for (int i = 0; i < trapezoids.size(); i++) {
                output.write("T" + (i + 1) + " ");
            }
            output.write("Sum\n");
            for (int i = 0; i < adjacencyMatrix.size(); i++) {
                for (int j = 0; j < adjacencyMatrix.size(); j++) {
                    if (j == 0) {
                        if (i < n) output.write("P" + (i + 1) + "  ");
                        else if (i < 2 * n) output.write("Q" + (i - n + 1) + "  ");
                        else if (i < 3 * n) output.write("S" + (i - 2 * n + 1) + "  ");
                        else if (i < 3 * n + trapezoids.size()) {
                            if (i - 3 * n + 1 < 10) output.write("T" + (i - 3 * n + 1) + "  ");
                            else output.write("T" + (i - 3 * n + 1) + " ");
                        } else output.write("Sum ");
                    }
                    if (j - 3 * n + 1 > 10) output.write("  " + adjacencyMatrix.get(i).get(j) + " ");
                    else output.write(" " + adjacencyMatrix.get(i).get(j) + " ");
                }
                output.write("\n");
            }
        } catch (IOException e) {
            System.out.println("Output error!");
        }
    }

    /**
     * This method accepts an input point from the command line in the form
     * x-value y-value (separated by a space). Once the user enters the x
     * and y value, the corresponding traversal path in the binary tree will
     * be printed as a string in the command line.
     */
    public void query() {
        Scanner in = new Scanner(System.in);
        double x, y;
        String[] aStringArray;
        while (true) {
            try {
                System.out.print("x y: ");
                aStringArray = in.nextLine().split(" ");
                x = Double.parseDouble(aStringArray[0]);
                y = Double.parseDouble(aStringArray[1]);
                traversePath(x, y, tree.root);
                System.out.println("\n");
            } catch (Exception e) {
                System.out.println("Error! Input is not a number or some other input error.");
                System.out.println("Please restart the program and retry.");
                break;
            }
        }
        in.close();
    }

    /**
     * This is a helper function of query() that traverses the tree to find the traversal path.
     *
     * @param x       - x-coordinate of the input point
     * @param y       - y-coordinate of the input point
     * @param current - the current node in the tree
     */
    public void traversePath(double x, double y, Node current) {
        System.out.print(current.type + current.index + " ");
        switch (current.type) {
            case "P":
                if (x >= segments.get(current.index - 1)[0]) traversePath(x, y, current.right);
                else traversePath(x, y, current.left);
                break;
            case "Q":
                if (x >= segments.get(current.index - 1)[2]) traversePath(x, y, current.right);
                else traversePath(x, y, current.left);
                break;
            case "S":
                double x1 = segments.get(current.index - 1)[0];
                double y1 = segments.get(current.index - 1)[1];
                double x2 = segments.get(current.index - 1)[2];
                double y2 = segments.get(current.index - 1)[3];
                double yTemp = (y1 * (x2 - x) + y2 * (x - x1)) / (x2 - x1);
                if (y >= yTemp) traversePath(x, y, current.left);
                else traversePath(x, y, current.right);
                break;
        }
    }

    /**
     * Main method.
     *
     * @param args - command line arguments (input_filename)
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: TrapezoidalMap.java input_filename");
            System.exit(0);
        }
        TrapezoidalMap hw3 = new TrapezoidalMap();
        hw3.input(args[0]);
        hw3.incremental();
        hw3.buildMatrix();
        hw3.output();
        hw3.query();
    }
}

/**
 * This is an auxiliary class which represents a node in a tree (trapezoidal map).
 */
class Node {
    /**
     * the type of this node (P, Q, S, or T)
     */
    String type;

    /**
     * the index of this node
     */
    int index;

    /**
     * the left child of this node
     */
    Node left;

    /**
     * the right child of this node
     */
    Node right;

    /**
     * The constructor initializes some important fields.
     *
     * @param type  - type of the node
     * @param index - index of the node
     */
    public Node(String type, int index) {
        this.type = type;
        this.index = index;
    }
}

/**
 * This is an auxiliary class which represents a tree (trapezoidal map)
 */
class MyTree {
    /**
     * the root node of this tree
     */
    public Node root;
}