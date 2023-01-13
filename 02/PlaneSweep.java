/*
 * This file illustrates PlaneSweep.java from assignment 2.
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * This program first takes its first system argument as the filename of the input file
 * and reads the line segments information from the file. After that. it performs plane
 * sweep algorithm to find all intersections of the segments. At last, total number of
 * intersections and all their coordinates are printed to the standard output and also
 * out put as a text file, named "intersections.txt". In addition, all line segments
 * with their end points and all intersections are visualized and output as an image,
 * named "visualization.png".
 *
 * @author Michael Lee, ml3406@rit.edu
 */
public class PlaneSweep {
    /**
     * a list to store the information of all line segments
     */
    List<Point[]> lineSegments;

    /**
     * a tree set to store all events with priority
     */
    TreeSet<Point> events;

    /**
     * sweep line status
     */
    TreeMap<Double, Point> SLS;

    /**
     * a list to store all found intersections
     */
    List<Point> intersections;

    /**
     * The constructor initializes some important fields.
     */
    public PlaneSweep() {
        lineSegments = new ArrayList<>();
        events = new TreeSet<>(new ComparatorForEvents());
        SLS = new TreeMap<>();
        intersections = new ArrayList<>();
    }

    /**
     * This method reads the information of all line segments from a text
     * file and store the information to the corresponding data structure.
     *
     * @param filename the input filename
     */
    public void input(String filename) {
        try (BufferedReader input = new BufferedReader(new FileReader(filename))) {
            // number of line segments
            int n = Integer.parseInt(input.readLine());
            String segment;
            String[] splitSegment;
            double x1, y1, x2, y2;

            // left and right end points of a line segment
            Point pointLeft, pointRight;
            for (int i = 0; i < n; i++) {
                segment = input.readLine();
                splitSegment = segment.split(" ");
                x1 = Double.parseDouble(splitSegment[0]);
                y1 = Double.parseDouble(splitSegment[1]);
                x2 = Double.parseDouble(splitSegment[2]);
                y2 = Double.parseDouble(splitSegment[3]);

                // left end point has lower x-coordinate value
                if (x1 < x2) {
                    pointLeft = new Point(x1, y1, "left");
                    pointRight = new Point(x2, y2, "right");
                } else {
                    pointLeft = new Point(x2, y2, "left");
                    pointRight = new Point(x1, y1, "right");
                }

                // add references of left/right endpoints to right/left end points
                pointLeft.p2 = pointRight;
                pointRight.p1 = pointLeft;
                lineSegments.add(new Point[]{pointLeft, pointRight});
            }
        } catch (IOException e) {
            System.out.println("File not found or input error!");
        }
    }

    /**
     * This method first calculates every line segment's slope (a) and y-intercept (b), and
     * then adds all events (all end points) to the tree set. After that, it performs plane
     * sweep algorithm, which deals with all upcoming events one at a time accordingly and
     * finds all intersections.
     */
    void myAlgorithm() {
        // calculate slopes and y-intercepts
        calculateAB();

        // add all events to the tree set
        addEvents();

        // a double array to store x/y coordinates of possible intersections temporarily
        double[] intersection;

        // algorithm ends while there is no upcoming event
        do {
            // next upcoming event
            Point event = events.first();
            events.remove(event);

            // deal with different types of events
            switch (event.type) {
                // left end points
                case "left":
                    if (SLS.size() == 0) {
                        // the starting key
                        event.keySLS = 0.0;
                        SLS.put(event.keySLS, event);
                    } else {
                        // add to the bottom (new key = last key + 1.0)
                        if (event.y < SLS.get(SLS.lastKey()).y) {
                            event.keySLS = SLS.lastKey() + 1.0;
                            SLS.put(event.keySLS, event);

                            // check possible intersection with the line segment above
                            if (event.a > SLS.get(SLS.lastKey() - 1.0).a) {
                                intersection = calculateI(SLS.lastKey() - 1.0, SLS.lastKey());
                                Point eventI = new Point(intersection[0], intersection[1], "intersection");
                                eventI.p1 = SLS.get(SLS.lastKey() - 1.0);
                                eventI.p2 = event;
                                events.add(eventI);
                            }
                        }

                        // add to the top (new key = last key - 1.0)
                        else if (event.y > SLS.get(SLS.firstKey()).y) {
                            event.keySLS = SLS.firstKey() - 1.0;
                            SLS.put(event.keySLS, event);

                            // check possible intersection with the line segment below
                            if (event.a < SLS.get(SLS.firstKey() + 1.0).a) {
                                intersection = calculateI(SLS.firstKey(), SLS.firstKey() + 1.0);
                                Point eventI = new Point(intersection[0], intersection[1], "intersection");
                                eventI.p1 = event;
                                eventI.p2 = SLS.get(SLS.firstKey() + 1.0);
                                events.add(eventI);
                            }
                        }

                        // insert
                        else {
                            // use binary search to find the proper key to insert a new line segment to SLS
                            event.keySLS = findKey(SLS.firstKey(), SLS.lastKey(), event.x, event.y);
                            SLS.put(event.keySLS, event);

                            // remove possible intersection of the line segments above and below if there is one
                            if (SLS.get(SLS.higherKey(event.keySLS)).a > SLS.get(SLS.lowerKey(event.keySLS)).a) {
                                intersection = calculateI(SLS.lowerKey(event.keySLS), SLS.higherKey(event.keySLS));
                                events.remove(new Point(intersection[0], intersection[1], "intersection"));
                            }

                            // check possible intersection with the line segment above
                            if (event.a > SLS.get(SLS.lowerKey(event.keySLS)).a) {
                                intersection = calculateI(SLS.lowerKey(event.keySLS), event.keySLS);
                                Point eventI = new Point(intersection[0], intersection[1], "intersection");
                                eventI.p1 = SLS.get(SLS.lowerKey(event.keySLS));
                                eventI.p2 = event;
                                events.add(eventI);
                            }
                            // check possible intersection with the line segment below
                            if (event.a < SLS.get(SLS.higherKey(event.keySLS)).a) {
                                intersection = calculateI(event.keySLS, SLS.higherKey(event.keySLS));
                                Point eventI = new Point(intersection[0], intersection[1], "intersection");
                                eventI.p1 = event;
                                eventI.p2 = SLS.get(SLS.higherKey(event.keySLS));
                                events.add(eventI);
                            }
                        }
                    }
                    break;

                // right end points
                case "right":
                    // use reference to acquire its left endpoint's key
                    double key = event.p1.keySLS;
                    if (SLS.size() != 1) {
                        // add or remove possible intersections with adjacent line segments accordingly
                        if (key == SLS.firstKey()) {
                            if (event.p1.a < SLS.get(SLS.higherKey(key)).a) {
                                intersection = calculateI(key, SLS.higherKey(key));
                                events.remove(new Point(intersection[0], intersection[1], "intersection"));
                            }
                        } else if (key == SLS.lastKey()) {
                            if (event.p1.a > SLS.get(SLS.lowerKey(key)).a) {
                                intersection = calculateI(SLS.lowerKey(key), key);
                                events.remove(new Point(intersection[0], intersection[1], "intersection"));
                            }
                        } else {
                            if (event.p1.a < SLS.get(SLS.higherKey(key)).a) {
                                intersection = calculateI(key, SLS.higherKey(key));
                                events.remove(new Point(intersection[0], intersection[1], "intersection"));
                            }
                            if (event.p1.a > SLS.get(SLS.lowerKey(key)).a) {
                                intersection = calculateI(SLS.lowerKey(key), key);
                                events.remove(new Point(intersection[0], intersection[1], "intersection"));
                            }
                            if (SLS.get(SLS.lowerKey(key)).a < SLS.get(SLS.higherKey(key)).a) {
                                intersection = calculateI(SLS.lowerKey(key), SLS.higherKey(key));
                                Point eventI = new Point(intersection[0], intersection[1], "intersection");
                                eventI.p1 = SLS.get(SLS.lowerKey(key));
                                eventI.p2 = SLS.get(SLS.higherKey(key));
                                events.add(eventI);
                            }
                        }
                    }
                    // finish checking a line segment (remove it from SLS)
                    SLS.remove(key);
                    break;

                // intersections
                case "intersection":
                    Point upperSegment = event.p1, lowerSegment = event.p2;
                    double key1 = upperSegment.keySLS, key2 = lowerSegment.keySLS;

                    // add or remove possible intersections accordingly
                    if (key1 != SLS.firstKey()) {
                        if (SLS.get(SLS.lowerKey(key1)).a < SLS.get(key1).a) {
                            intersection = calculateI(SLS.lowerKey(key1), key1);
                            events.remove(new Point(intersection[0], intersection[1], "intersection"));
                        }
                        if (SLS.get(SLS.lowerKey(key1)).a < SLS.get(key2).a) {
                            intersection = calculateI(SLS.lowerKey(key1), key2);
                            Point eventI = new Point(intersection[0], intersection[1], "intersection");
                            eventI.p1 = SLS.get(SLS.lowerKey(key1));
                            eventI.p2 = SLS.get(key2);
                            events.add(eventI);
                        }
                    }
                    if (key2 != SLS.lastKey()) {
                        if (SLS.get(key2).a < SLS.get(SLS.higherKey(key2)).a) {
                            intersection = calculateI(key2, SLS.higherKey(key2));
                            events.remove(new Point(intersection[0], intersection[1], "intersection"));
                        }
                        if (SLS.get(key1).a < SLS.get(SLS.higherKey(key2)).a) {
                            intersection = calculateI(key1, SLS.higherKey(key2));
                            Point eventI = new Point(intersection[0], intersection[1], "intersection");
                            eventI.p1 = SLS.get(key1);
                            eventI.p2 = SLS.get(SLS.higherKey(key2));
                            events.add(eventI);
                        }
                    }

                    // since an intersection happens between two segments, we have to swap their positions in SLS
                    upperSegment.keySLS = key2;
                    lowerSegment.keySLS = key1;
                    SLS.put(key1, lowerSegment);
                    SLS.put(key2, upperSegment);
                    intersections.add(event);
                    break;
            }
        } while (events.size() != 0);
    }

    /**
     * This method is a helper function of myAlgorithm that calculates
     * slopes (a) and y-intercepts (b) of all line segments.
     */
    void calculateAB() {
        for (Point[] p : lineSegments) {
            p[0].a = (p[0].y - p[1].y) / (p[0].x - p[1].x);
            p[0].b = p[0].y - p[0].a * p[0].x;
            p[1].a = p[0].a;
            p[1].b = p[0].b;
        }
    }

    /**
     * This method is a helper function of myAlgorithm
     * that adds all initial events to the tree set.
     */
    void addEvents() {
        for (Point[] p : lineSegments) {
            events.add(p[0]);
            events.add(p[1]);
        }
    }

    /**
     * This method is a helper function of myAlgorithm that
     * performs binary search to find the proper key for the
     * newly encountered line segment.
     *
     * @param higherYKey upper bound key
     * @param lowerYKey  lower bound key
     * @param x          current x-coordinate of the sweep line
     * @param y          y-coordinate of the newly encountered left end point
     * @return the key to insert the line segment to SLS
     */
    double findKey(double higherYKey, double lowerYKey, double x, double y) {
        double result;
        double yCurrent, yHigher, yLower;
        boolean keepFinding = true;
        do {
            result = (higherYKey + lowerYKey) / 2;
            if (SLS.containsKey(result)) {
                // y = ax + b
                yCurrent = SLS.get(result).a * x + SLS.get(result).b;
                if (y >= yCurrent) lowerYKey = result;
                else higherYKey = result;
            } else {
                // y = ax + b
                yHigher = SLS.get(SLS.lowerKey(result)).a * x + SLS.get(SLS.lowerKey(result)).b;
                yLower = SLS.get(SLS.higherKey(result)).a * x + SLS.get(SLS.higherKey(result)).b;
                if ((y == yHigher && y == yLower) || (y > yHigher && y > yLower)) lowerYKey = result;
                else if (y < yHigher && y < yLower) higherYKey = result;
                else keepFinding = false;
            }
        } while (keepFinding);
        return result;
    }

    /**
     * This method is a helper function of myAlgorithm that calculates
     * the x/y coordinates of the possible intersection.
     *
     * @param key1 the key of upper line segment of the intersection
     * @param key2 the key of lower line segment of the intersection
     * @return the x/y coordinates of the possible intersection
     */
    double[] calculateI(double key1, double key2) {
        // solve x and y from y = a1 x + b1 and y = a2 x + b2
        double a1 = SLS.get(key1).a;
        double b1 = SLS.get(key1).b;
        double a2 = SLS.get(key2).a;
        double b2 = SLS.get(key2).b;
        double x = (b2 - b1) / (a1 - a2);
        double y = a1 * x + b1;
        return new double[]{x, y};
    }

    /**
     * This method deals with output tasks.
     */
    public void output() {
        txt();
        png();
    }

    /**
     * This method is a helper function of output() that prints the total number of
     * intersections and all their coordinates to the standard output and also to a
     * text file, named "intersections.txt".
     */
    public void txt() {
        try (BufferedWriter output = new BufferedWriter(new FileWriter("intersections.txt"))) {
            System.out.println(intersections.size());
            output.write(intersections.size() + "\n");
            for (Point p : intersections) {
                System.out.println(p);
                output.write(p + "\n");
            }
        } catch (IOException e) {
            System.out.println("Output error!");
        }
    }

    /**
     * This method is a helper function that visualizes all line segments, their
     * end points, and all intersections as an image, named "visualization.png".
     */
    public void png() {
        // calculate the size of the image according to the ranges of x and y
        int xMax = 0, xMin = 0, yMax = 0, yMin = 0;
        for (int i = 0; i < lineSegments.size(); i++) {
            if (i == 0) {
                if (lineSegments.get(i)[0].x > lineSegments.get(i)[1].x) {
                    xMax = round(lineSegments.get(i)[0].x);
                    xMin = round(lineSegments.get(i)[1].x);
                } else {
                    xMax = round(lineSegments.get(i)[1].x);
                    xMin = round(lineSegments.get(i)[0].x);
                }
                if (lineSegments.get(i)[0].y > lineSegments.get(i)[1].y) {
                    yMax = round(lineSegments.get(i)[0].y);
                    yMin = round(lineSegments.get(i)[1].y);
                } else {
                    yMax = round(lineSegments.get(i)[1].y);
                    yMin = round(lineSegments.get(i)[0].y);
                }
            } else {
                if (lineSegments.get(i)[0].x > xMax) xMax = round(lineSegments.get(i)[0].x);
                if (lineSegments.get(i)[1].x > xMax) xMax = round(lineSegments.get(i)[1].x);
                if (lineSegments.get(i)[0].x < xMin) xMin = round(lineSegments.get(i)[0].x);
                if (lineSegments.get(i)[1].x < xMin) xMin = round(lineSegments.get(i)[1].x);
                if (lineSegments.get(i)[0].y > yMax) yMax = round(lineSegments.get(i)[0].y);
                if (lineSegments.get(i)[1].y > yMax) yMax = round(lineSegments.get(i)[1].y);
                if (lineSegments.get(i)[0].y < yMin) yMin = round(lineSegments.get(i)[0].y);
                if (lineSegments.get(i)[1].y < yMin) yMin = round(lineSegments.get(i)[1].y);
            }
        }
        int width = xMax - xMin + 177, height = yMax - yMin + 147;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = image.createGraphics();

        // background
        graph.setColor(Color.white);
        graph.fillRect(0, 0, width, height);

        // frame of the graph
        graph.setColor(Color.black);
        graph.drawLine(71, 71, 71, 77 + yMax - yMin);
        graph.drawLine(71, 77 + yMax - yMin, 77 + xMax - xMin, 77 + yMax - yMin);
        graph.drawLine(77 + xMax - xMin, 77 + yMax - yMin, 77 + xMax - xMin, 71);
        graph.drawLine(77 + xMax - xMin, 71, 71, 71);

        // draw coordinates
        int digit = digit(yMax);
        graph.drawString(Integer.toString(yMax), 69 - digit * 7, 75);
        digit = digit(yMin);
        graph.drawString(Integer.toString(yMin), 69 - digit * 7, 76 + yMax - yMin);
        digit = digit(xMin);
        graph.drawString(Integer.toString(xMin), 71 - digit * 7 / 2, 90 + yMax - yMin);
        digit = digit(xMax);
        graph.drawString(Integer.toString(xMax), 72 + xMax - xMin - digit * 7 / 2, 90 + yMax - yMin);

        // draw legend
        graph.setColor(Color.blue);
        graph.drawLine(88 + xMax - xMin, -32 + width / 2, 92 + xMax - xMin, -32 + width / 2);
        graph.setColor(Color.black);
        graph.drawString("line segment", 98 + xMax - xMin, -27 + width / 2);
        graph.setColor(Color.green);
        graph.fillRect(88 + xMax - xMin, -12 + width / 2, 5, 5);
        graph.setColor(Color.black);
        graph.drawString("end point", 98 + xMax - xMin, -6 + width / 2);
        graph.setColor(Color.red);
        graph.fillRect(88 + xMax - xMin, 8 + width / 2, 5, 5);
        graph.setColor(Color.black);
        graph.drawString("intersection", 98 + xMax - xMin, 14 + width / 2);


        // draw line segments with their end points
        for (Point[] p : lineSegments) {
            graph.setColor(Color.blue);
            graph.drawLine(((int) p[0].x) + 74 - xMin, 74 + yMax - ((int) p[0].y),
                    ((int) p[1].x) + 74 - xMin, 74 + yMax - ((int) p[1].y));
            graph.setColor(Color.green);
            graph.fillRect(((int) p[0].x) + 73 - xMin, 73 + yMax - ((int) p[0].y), 3, 3);
            graph.fillRect(((int) p[1].x) + 73 - xMin, 73 + yMax - ((int) p[1].y), 3, 3);
        }

        // draw intersections
        graph.setColor(Color.red);
        int x, y;
        for (Point p : intersections) {
            x = round(p.x);
            y = round(p.y);
            graph.fillRect(x + 73 - xMin, 73 + yMax - y, 3, 3);
        }

        graph.dispose();
        File file = new File("visualization.png");

        // output the image
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            System.out.println("IO errors.");
        }


    }

    /**
     * This method is a helper function of png() that rounds up/down double to integer.
     *
     * @param value the double value to be rounded up/down
     * @return the integer value
     */
    public int round(double value) {
        if (value % 1.0 >= 0.5) return (int) (value + 1.0);
        else return (int) value;
    }

    /**
     * This method is a helper function of png() that calculates the number of digit of a given integer.
     *
     * @param value the given integer
     * @return the number of digit
     */
    public int digit(int value) {
        int result = 0;
        int temp1 = value, temp2 = value;
        while (true) {
            result++;
            temp2 = temp2 % 10;
            if (temp1 == temp2) break;
            else temp1 = temp2;
        }
        return result;
    }

    /**
     * Main method.
     *
     * @param args command line arguments -- input_filename
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: PlaneSweep.java input_filename");
            System.exit(0);
        }
        PlaneSweep hw2 = new PlaneSweep();
        hw2.input(args[0]);
        hw2.myAlgorithm();
        hw2.output();
    }
}

/**
 * This is an auxiliary class that represents points (end points/intersections).
 */
class Point {
    /**
     * x coordinate
     */
    double x;
    /**
     * y coordinate
     */
    double y;
    /**
     * slope
     */
    double a;
    /**
     * y-intercept
     */
    double b;
    /**
     * key used for SLS (for left end points)
     */
    double keySLS;
    /**
     * type of the point (left/right end point or intersection)
     */
    String type;
    /**
     * references:
     * p1 as left end point for right end point
     * p2 as right end point for left end point
     * p1 as upper segment and p2 as lower segment for intersection
     */
    Point p1, p2;

    /**
     * The constructor initializes some important fields.
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param type ype of the point (left/right end point or intersection)
     */
    public Point(double x, double y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /**
     * Returns a String representation of the point, suitable for printing.
     *
     * @return a String representation of the point
     */
    @Override
    public String toString() {
        return x + " " + y;
    }

    /**
     * This method returns the equality between this Point and another object
     * by comparing their x coordinates, y coordinates, and types.
     *
     * @param o the object to be compared with this Point
     * @return boolean if two objects are the same
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return (x == p.x && y == p.y && type.equals(p.type));
    }

    /**
     * This method computes and returns the hash code of this Point.
     *
     * @return the hash code of this Point
     */
    @Override
    public int hashCode() {
        return (int) (x + y + x * y + type.hashCode());
    }
}

/**
 * an auxiliary class used as comparator for the tree set (sorted by x coordinates)
 */
class ComparatorForEvents implements Comparator<Point> {
    @Override
    public int compare(Point p1, Point p2) {
        if (p1.x == p2.x && p1.y == p2.y && p1.type.equals(p2.type)) return 0;
        if (p1.x < p2.x) return -1;
        else if (p1.x > p2.x) return 1;
        else {
            if (p1.y == p2.y) {
                if (p1.type.equals("intersection")) return -1;
                else return 1;
            } else return -1;
        }
    }
}