/*
 * This file illustrates ConvexHullBruteForce.java from assignment 1.
 */

import java.io.*;
import java.util.*;

/**
 * This program first takes its first system argument as the filename of the input file and
 * reads the data points information from the file. After that. it finds all the points that
 * are in the convex hull by brute force. At Last, total number of points in the hull and all
 * those points' coordinates are printed to the standard output and also output as a text file,
 * named after the second system argument. In addition, total time spent of the brute force
 * algorithm is being recorded and printed on the standard output.
 *
 * @author Michael Lee, ml3406@rit.edu
 */
public class ConvexHullBruteForce {
    /** number of input data points */
    int n;

    /** a list to store all data points' x-y coordinates */
    List<int[]> vertices;

    /** a list to store all points in the hull in counter-clock wise order */
    List<String> sortedVertices;

    /** a set to store all points in the hull */
    Set<String> avoidDuplicate;

    /**
     * The constructor initializes some important fields.
     */
    public ConvexHullBruteForce() {
        vertices = new ArrayList<>();
        sortedVertices = new ArrayList<>();
        avoidDuplicate = new LinkedHashSet<>();
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
     * This method finds all points belonging the hull by brute force.
     */
    public void convexHull() {
        // auxiliary variables
        int x1, y1, x2, y2, x3, y3;
        int value = 0;
        boolean first, belong, collinear1, collinear2;

        // choose every two points to form a edge and check if that edge belongs to the convex hull
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                x1 = vertices.get(i)[0];
                y1 = vertices.get(i)[1];
                x2 = vertices.get(j)[0];
                y2 = vertices.get(j)[1];

                // determinant
                int det;
                first = true;
                belong = true;

                // check if all other points are on the same side of edge
                for (int k = 0; k < n; k++) {
                    if (k != i && k != j) {
                        x3 = vertices.get(k)[0];
                        y3 = vertices.get(k)[1];
                        det = x2 * y3 + x1 * y2 + y1 * x3 - x2 * y1 - x3 * y2 - y3 * x1;
                        collinear1 = x3 >= x1 && x3 >= x2 && y3 >= y1 && y3 >= y2;
                        collinear2 = x3 <= x1 && x3 <= x2 && y3 <= y1 && y3 <= y2;
                        if (first) {
                            if (det == 0) {
                                if (collinear1 || collinear2) {
                                    belong = false;
                                }
                            } else {
                                value = det;
                                first = false;
                            }
                        } else {
                            // there are points on different sides of the edge
                            if (value > 0 && det < 0 || value < 0 && det > 0) {
                                belong = false;
                            }
                            // a point is collinear with the the two chosen points
                            else if (det == 0) {
                                if (collinear1 || collinear2) {
                                    belong = false;
                                }
                            }
                        }
                    }
                }

                // add points belonging to the hull to a set
                if (belong) {
                    avoidDuplicate.add(x1 + " " + y1);
                    avoidDuplicate.add(x2 + " " + y2);
                }
            }
        }
    }

    /**
     * This method sorts all points in the hull in a counter-clock wise order to a list.
     */
    public void sortCounterClockWise() {
        int xMax = -2147483648, yOfxMax = 2147483647;
        int xTemp, yTemp, xSlopeMin = 0, ySlopeMin = 0;
        double slope, slopeMin = 0.0;
        boolean first;

        // find the xMax point
        for (String s : avoidDuplicate) {
            xTemp = Integer.parseInt(s.split(" ")[0]);
            yTemp = Integer.parseInt(s.split(" ")[1]);
            if (xTemp > xMax || (xTemp == xMax && yTemp < yOfxMax)) {
                xMax = xTemp;
                yOfxMax = yTemp;
            }
        }
        avoidDuplicate.remove(xMax + " " + yOfxMax);
        sortedVertices.add(xMax + " " + yOfxMax);

        // similar to gift wrapping but using slope
        while (avoidDuplicate.size() != 0) {
            first = true;
            for (String s : avoidDuplicate) {
                xTemp = Integer.parseInt(s.split(" ")[0]);
                yTemp = Integer.parseInt(s.split(" ")[1]);

                // avoid infinity slope
                if (xTemp == xMax) {
                    xSlopeMin = xTemp;
                    ySlopeMin = yTemp;
                    break;
                } else {
                    slope = ((double) yTemp - (double) yOfxMax) / ((double) xTemp - (double) xMax);
                    if (first) {
                        first = false;
                        xSlopeMin = xTemp;
                        ySlopeMin = yTemp;
                        slopeMin = slope;
                    } else {
                        if (slope < slopeMin) {
                            xSlopeMin = xTemp;
                            ySlopeMin = yTemp;
                            slopeMin = slope;
                        }
                    }
                }
            }
            avoidDuplicate.remove(xSlopeMin + " " + ySlopeMin);
            sortedVertices.add(xSlopeMin + " " + ySlopeMin);
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
            output.write(sortedVertices.size() + "\n");
            System.out.println(sortedVertices.size());
            for (String s : sortedVertices) {
                System.out.println(s);
                output.write(s + "\n");
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
            System.out.println("Usage: ConvexHullBruteForce.java input_file_name output_file_name");
            System.exit(0);
        }
        ConvexHullBruteForce hw1 = new ConvexHullBruteForce();
        hw1.input(args[0]);
        long startTime = System.nanoTime();
        hw1.convexHull();
        hw1.sortCounterClockWise();
        long endTime = System.nanoTime();
        hw1.output(args[1]);
        System.out.println("Time spent: " + (endTime - startTime) / 1000000000.0 + " seconds.");
    }
}
