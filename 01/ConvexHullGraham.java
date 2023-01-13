/*
 * This file illustrates ConvexHullGraham.java from assignment 1.
 */

import java.io.*;
import java.util.*;

/**
 * This program first takes its first system argument as the filename of the input file and
 * reads the data points information from the file. After that. it performs Graham's Scan
 * to find all the points that are in the convex hull. At Last, total number of points in
 * the hull and all those points' coordinates are printed to the standard output and also
 * output as a text file, named after the second system argument. In addition, total time
 * spent of the Graham's Scan is being recorded and printed on the standard output.
 *
 * @author Michael Lee, ml3406@rit.edu
 */
public class ConvexHullGraham {
    /** number of input data points */
    int n;

    /** a list to store all data points' x-y coordinates */
    List<int[]> vertices;

    /** a list to store upper hull's points in left-hand turn order */
    List<Integer> upperHull;

    /** a list to store lower hull's points in right-hand turn order */
    List<Integer> lowerHull;

    /** a list to store all points in the hull in counter-clock wise order */
    List<Integer> sortedVerticesConvexHull;

    /**
     * The constructor initializes some important fields.
     */
    public ConvexHullGraham() {
        vertices = new ArrayList<>();
        upperHull = new ArrayList<>();
        lowerHull = new ArrayList<>();
        sortedVerticesConvexHull = new ArrayList<>();
    }

    /**
     * This method reads the input data points from a text
     * file and store them to corresponding data structures.
     *
     * @param fileName the input filename
     */
    public void input(String fileName) {
        try (BufferedReader input = new BufferedReader(new FileReader(fileName))) {
            n = Integer.parseInt(input.readLine());
            String vertex1;
            String[] vertex2;
            while ((vertex1 = input.readLine()) != null) {
                vertex2 = vertex1.split(" ");
                vertices.add(new int[]{Integer.parseInt(vertex2[0]), Integer.parseInt(vertex2[1])});
            }
        } catch (IOException e) {
            System.out.println("File not found or input error!");
        }
    }

    /**
     * This method performs Graham's Scan to to find all points belonging the hull.
     */
    public void convexHull() {
        // sort all data points by there x-coordinates
        vertices = sortByX(0, n - 1);

        // 3 most recently added data points to the upper and lower hulls
        int[] u1, u2, u3, l1, l2, l3;

        // determinants
        int uDet, lDet;

        // auxiliary variables to check collinearity
        boolean collinear1, collinear2;

        // add first two points
        upperHull.add(0);
        lowerHull.add(0);
        upperHull.add(1);
        lowerHull.add(1);
        u1 = vertices.get(0);
        l1 = vertices.get(0);
        u2 = vertices.get(1);
        l2 = vertices.get(1);

        // establish upper hull (start from the third point)
        int index = 2;
        while (index < n) {
            u3 = vertices.get(index);
            uDet = u2[0] * u3[1] + u1[0] * u2[1] + u1[1] * u3[0] - u2[0] * u1[1] - u3[0] * u2[1] - u3[1] * u1[0];

            // follow left-hand turn
            if (uDet > 0) {
                u1 = u2;
                u2 = u3;
                upperHull.add(index);
                index++;
            }

            // deal with collinear situation
            else if (uDet == 0) {
                collinear1 = u3[0] >= u1[0] && u3[0] >= u2[0] && u3[1] >= u1[1] && u3[1] >= u2[1];
                collinear2 = u3[0] <= u1[0] && u3[0] <= u2[0] && u3[1] <= u1[1] && u3[1] <= u2[1];
                if (collinear1 || collinear2) {
                    upperHull.remove(upperHull.size() - 1);
                    if (upperHull.size() == 1) {
                        upperHull.add(index);
                        u2 = u3;
                        index++;
                    } else {
                        u2 = u1;
                        u1 = vertices.get(upperHull.get(upperHull.size() - 2));
                    }
                } else {
                    index++;
                }
            }

            // violate left-hand rule
            else {
                upperHull.remove(upperHull.size() - 1);
                if (upperHull.size() == 1) {
                    upperHull.add(index);
                    u2 = u3;
                    index++;
                } else {
                    u2 = u1;
                    u1 = vertices.get(upperHull.get(upperHull.size() - 2));
                }
            }
        }

        // establish lower hull (similar to upper hull)
        index = 2;
        while (index < n) {
            l3 = vertices.get(index);
            lDet = l2[0] * l3[1] + l1[0] * l2[1] + l1[1] * l3[0] - l2[0] * l1[1] - l3[0] * l2[1] - l3[1] * l1[0];
            if (lDet < 0) {
                l1 = l2;
                l2 = l3;
                lowerHull.add(index);
                index++;
            } else if (lDet == 0) {
                collinear1 = l3[0] >= l1[0] && l3[0] >= l2[0] && l3[1] >= l1[1] && l3[1] >= l2[1];
                collinear2 = l3[0] <= l1[0] && l3[0] <= l2[0] && l3[1] <= l1[1] && l3[1] <= l2[1];
                if (collinear1 || collinear2) {
                    lowerHull.remove(lowerHull.size() - 1);
                    if (lowerHull.size() == 1) {
                        lowerHull.add(index);
                        l2 = l3;
                        index++;
                    } else {
                        l2 = l1;
                        l1 = vertices.get(lowerHull.get(lowerHull.size() - 2));
                    }
                } else {
                    index++;
                }
            } else {
                lowerHull.remove(lowerHull.size() - 1);
                if (lowerHull.size() == 1) {
                    lowerHull.add(index);
                    l2 = l3;
                    index++;
                } else {
                    l2 = l1;
                    l1 = vertices.get(lowerHull.get(lowerHull.size() - 2));
                }
            }
        }
    }

    /**
     * This method performs mergesort for sorting all data points by their x-coordinates.
     *
     * @param start the index of the first element
     * @param end   the index of the last element
     * @return a list of sorted points
     */
    public List<int[]> sortByX(int start, int end) {
        List<int[]> temp = new ArrayList<>();
        if (start != end) {
            int middle = (start + end) / 2;

            // divide
            List<int[]> left = sortByX(start, middle);
            List<int[]> right = sortByX(middle + 1, end);
            int leftIndex = 0, rightIndex = 0;

            // merge
            do {
                if (left.get(leftIndex)[0] >= right.get(rightIndex)[0]) {
                    temp.add(left.get(leftIndex));
                    leftIndex++;
                } else {
                    temp.add(right.get(rightIndex));
                    rightIndex++;
                }
            } while (leftIndex < left.size() && rightIndex < right.size());

            while (leftIndex < left.size()) {
                temp.add(left.get(leftIndex));
                leftIndex++;
            }
            while (rightIndex < right.size()) {
                temp.add(right.get(rightIndex));
                rightIndex++;
            }
        } else temp.add(vertices.get(start));
        return temp;
    }

    /**
     * This method combines all points from the upper hull and the lower hull accordingly.
     */
    public void combine() {
        // check if there are collinear points at the rightmost or the left most edges
        boolean checkRightCollinear = false, checkLeftCollinear = false;
        int upperXFirst = vertices.get(upperHull.get(0))[0];
        int upperXSecond = vertices.get(upperHull.get(1))[0];
        int lowerXFirst = vertices.get(lowerHull.get(0))[0];
        int lowerXSecond = vertices.get(lowerHull.get(1))[0];
        int upperXLast = vertices.get(upperHull.get(upperHull.size() - 1))[0];
        int upperXSecondLast = vertices.get(upperHull.get(upperHull.size() - 2))[0];
        int lowerXLast = vertices.get(lowerHull.get(lowerHull.size() - 1))[0];
        int lowerXSecondLast = vertices.get(lowerHull.get(lowerHull.size() - 2))[0];
        if (upperXFirst == upperXSecond && lowerXFirst == lowerXSecond) checkRightCollinear = true;
        if (upperXLast == upperXSecondLast && lowerXLast == lowerXSecondLast) checkLeftCollinear = true;

        // add vertices in upperHull to the final list accordingly
        for (int i = 0; i < upperHull.size(); i++) {
            if (checkRightCollinear && checkLeftCollinear) {
                if (i != 0 && i != upperHull.size() - 1) sortedVerticesConvexHull.add(upperHull.get(i));
            } else if (checkRightCollinear) {
                if (i != 0) sortedVerticesConvexHull.add(upperHull.get(i));
            } else if (checkLeftCollinear) {
                if (i != upperHull.size() - 1) sortedVerticesConvexHull.add(upperHull.get(i));
            } else sortedVerticesConvexHull.add(upperHull.get(i));
        }

        // add vertices in lowerHull to the final list
        for (int i = lowerHull.size() - 2; i > 0; i--) {
            sortedVerticesConvexHull.add(lowerHull.get(i));
        }
    }

    /**
     * This method prints and outputs the total numbers of points in the hull and
     * all those points' x-y coordinates to the standard output and as a text file.
     *
     * @param fileName the output filename
     */
    public void output(String fileName) {
        try (BufferedWriter output = new BufferedWriter(new FileWriter(fileName))) {
            System.out.println(sortedVerticesConvexHull.size());
            output.write(sortedVerticesConvexHull.size() + "\n");
            for (int i : sortedVerticesConvexHull) {
                System.out.println(vertices.get(i)[0] + " " + vertices.get(i)[1]);
                output.write(vertices.get(i)[0] + " " + vertices.get(i)[1] + "\n");
            }
        } catch (IOException e) {
            System.out.println("Output error!");
        }
    }

    /**
     * Main method.
     *
     * @param args command line arguments -- input_file_name output_file_name
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: ConvexHullGraham.java input_file_name output_file_name");
            System.exit(0);
        }
        ConvexHullGraham hw1 = new ConvexHullGraham();
        hw1.input(args[0]);
        long startTime = System.nanoTime();
        hw1.convexHull();
        hw1.combine();
        long endTime = System.nanoTime();
        hw1.output(args[1]);
        System.out.println("Time spent: " + (endTime - startTime) / 1000000000.0 + " seconds.");
    }
}
