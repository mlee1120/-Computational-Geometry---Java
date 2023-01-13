/*
 * This file illustrates convex_cell.pde (handles game Convex Cell) from my final project.
 *
 * @author Michael Lee, ml3406@rit.edu
 */

/** if there is a unit convex cell in on the screen or not */
boolean unitCell;

/** x/y coordinates of the center of the convex cell */
int[] centerOfConvexCell;

/** x/y coordinates of the center of the unit convex cell*/
int[] centerOfUnitConvexCell;

/** the pair of nodes that start merging the convex cell and the unit convex cell */
Node[] startNodes;

/** a LinkedList to store the positions of all nodes from the convex Cell in counter clockwise order */
LinkedList convexCell;

/** a LinkedList to store the positions of all nodes from the unit convex Cell in counter clockwise order */
LinkedList unitConvexCell;

/**
 * This function initializes all vaiables for the Convex Cell game mode.
 */
void setupConvexCell() {
  unitCell = false;
  centerOfConvexCell = new int[]{250, 250};
  centerOfUnitConvexCell = new int[2];
  startNodes = new Node[2];
  convexCell = new LinkedList();
  unitConvexCell = new LinkedList();
  generateConvexCell();
  generateUnitConvexCell();
}

/**
 * This function handles the in game frames for Convex Cell.
 */
void mainConvexCell() {
  if (!gameOver) {
    if (!unitCell) generateUnitConvexCell();
    moveHull();
    drawHulls();
    if (checkCollision()) combineCells();
    if (checkWall()) gameOver = true;
  } else {
    setupConvexCell();
    page = 2;
  }
}

/**
 * This function detects keypress in Convex Cell.
 */
void keyConvexCell() {
  if (gameOver) {
    if (keyCode == UP) restart = true;
    else if (keyCode == DOWN) restart = false;
    else if (keyCode == RIGHT && restart == false) gameMode = 0;
    else if (keyCode == RIGHT && restart == true) gameOver = false;
  }
}

/**
 * This function generates three vertices of the initial convex cell.
 */
void generateConvexCell() {
  convexCell.addLast(new int[]{235, 240}, 0, 0, 0);
  convexCell.addLast(new int[]{250, 270}, 0, 0, 0);
  convexCell.addLast(new int[]{265, 240}, 0, 0, 0);
}


/**
 * This function generates a unit convex cell at a random but valid position on the canvas.
 */
void generateUnitConvexCell() {
  unitConvexCell.clear();
  int[] vertex1 = new int[]{-15, -10};
  int[] vertex2 = new int[]{0, 15};
  int[] vertex3 = new int[]{15, -10};
  centerOfUnitConvexCell[0] = (vertex1[0] + vertex2[0] + vertex3[0]) / 3;
  centerOfUnitConvexCell[1] = (vertex1[1] + vertex2[1] + vertex3[1]) / 3;

  // random rotation
  int theta = (int) random(0, 359);
  int temp;
  temp = (int) (vertex1[0] * cos(theta * 2 * PI / 360) + vertex1[1] * sin(theta * 2 * PI / 360));
  vertex1[1] = (int) (vertex1[1] * cos(theta * 2 * PI / 360) - vertex1[0] * sin(theta * 2 * PI / 360));
  vertex1[0] = temp;
  temp = (int) (vertex2[0] * cos(theta * 2 * PI / 360) + vertex2[1] * sin(theta * 2 * PI / 360));
  vertex2[1] = (int) (vertex2[1] * cos(theta * 2 * PI / 360) - vertex2[0] * sin(theta * 2 * PI / 360));
  vertex2[0] = temp;
  temp = (int) (vertex3[0] * cos(theta * 2 * PI / 360) + vertex3[1] * sin(theta * 2 * PI / 360));
  vertex3[1] = (int) (vertex3[1] * cos(theta * 2 * PI / 360) - vertex3[0] * sin(theta * 2 * PI / 360));
  vertex3[0] = temp;

  calculateCenter();
  // max radius
  int radiusOfConvexHull = 0;
  int rTemp;
  Node n = convexCell.getPointer();
  for (int i = 0; i < convexCell.size(); i++) {
    rTemp = (int) sqrt((float) ((n.xy[0] - centerOfConvexCell[0]) * (n.xy[0] - centerOfConvexCell[0]) + (n.xy[1] - centerOfConvexCell[1]) * (n.xy[1] - centerOfConvexCell[1])));
    if (i == 0) radiusOfConvexHull = rTemp;
    else {
      if (rTemp > radiusOfConvexHull) radiusOfConvexHull = rTemp;
    }
    n = n.next;
  }

  int counter = 0;

  // random translation (at least a certain distance from the convex cell)
  int dx = 0, dy = 0, xTemp, yTemp;
  do {
    if (counter >= 30000) {
      gameOver = true;
      break;
    }
    dx = (int) random(75, 426);
    dy = (int) random(75, 426);
    xTemp = centerOfUnitConvexCell[0] + dx;
    yTemp = centerOfUnitConvexCell[1] + dy;
    counter++;
  } while ((xTemp - centerOfConvexCell[0]) * (xTemp - centerOfConvexCell[0]) + (yTemp - centerOfConvexCell[1]) * (yTemp - centerOfConvexCell[1]) < (10 + radiusOfConvexHull) * (10 + radiusOfConvexHull));

  vertex1[0] += dx;
  vertex1[1] += dy;
  vertex2[0] += dx;
  vertex2[1] += dy;
  vertex3[0] += dx;
  vertex3[1] += dy;

  // add vertices to the
  unitConvexCell.addLast(vertex1, 0, 0, 0);
  unitConvexCell.addLast(vertex2, 0, 0, 0);
  unitConvexCell.addLast(vertex3, 0, 0, 0);
  unitCell = true;
}

/**
 * This function performs translation of the convex cell according to the position of the mouse.
 */
void moveHull() {
  calculateCenter();
  int x = centerOfConvexCell[0];
  int y = centerOfConvexCell[1];
  float distance = sqrt((float) ((mouseX - x) * (mouseX - x) + (mouseY - y) * (mouseY - y)));
  int dx, dy;
  if (distance < 3) {
    dx = 0;
    dy = 0;
  } else {
    if (mousePressed) {
      dx = (int) (3 * ((float) mouseX - x) / distance);
      dy = (int) (3 * ((float) mouseY - y) / distance);
    } else {
      dx = (int) (2 * ((float) mouseX - x) / distance);
      dy = (int) (2 * ((float) mouseY - y) / distance);
    }
  }
  Node temp = convexCell.getPointer();
  for (int i = 0; i < convexCell.size(); i++) {
    temp.xy[0] += dx;
    temp.xy[1] += dy;
    temp = temp.next;
  }
}

/**
 * This function calculates the approximate center of the convex cell.
 */
void calculateCenter() {
  int xTotal = 0, yTotal = 0;
  Node temp = convexCell.getPointer();
  for (int i = 0; i < convexCell.size(); i++) {
    xTotal += temp.xy[0];
    yTotal += temp.xy[1];
    temp = temp.next;
  }
  centerOfConvexCell[0] = xTotal / convexCell.size();
  centerOfConvexCell[1] = yTotal / convexCell.size();
}

/**
 * This function draws the convex cell and the unit convex cell on the canvas.
 */
void drawHulls() {
  strokeWeight(3);
  Node temp;

  // draw convex cell
  stroke(255);
  fill(255);
  temp = convexCell.getPointer();
  for (int i = 0; i < convexCell.size(); i++) {
    if (i != convexCell.size() - 1) line(temp.xy[0], temp.xy[1], temp.next.xy[0], temp.next.xy[1]);
    else line(temp.xy[0], temp.xy[1], convexCell.first.xy[0], convexCell.first.xy[1]);
    ellipse(temp.xy[0], temp.xy[1], 6, 6);
    temp = temp.next;
  }

  // draw unit convex cell
  stroke(234, 234, 0);
  fill(234, 234, 0);
  temp = unitConvexCell.getPointer();
  for (int i = 0; i < unitConvexCell.size(); i++) {
    if (i != unitConvexCell.size() - 1) line(temp.xy[0], temp.xy[1], temp.next.xy[0], temp.next.xy[1]);
    else line(temp.xy[0], temp.xy[1], unitConvexCell.first.xy[0], unitConvexCell.first.xy[1]);
    ellipse(temp.xy[0], temp.xy[1], 6, 6);
    temp = temp.next;
  }
}

/**
 * This function checks and returns if there is a collision between the convex cell and the unit convex cell.
 * Technically, it preidcts a collision because there might be some problems to merge two cells if they are
 * actually collided (overlapped).
 * Algorithm: a collision is predicted if any vertex of the convex cell/the unit convex cell is in a certain range
 * of any edge of the unit convex cell/the convex cell.
 */
boolean checkCollision() {
  Node temp = convexCell.getPointer();
  Node node1 = unitConvexCell.getPointer(), node2 = node1.next, node3 = node2.next;
  for (int i = 0; i < convexCell.size(); i++) {
    // a vertex of the convex cell vs 3 edges of the unit convex cell
    if (checkDistance(temp, node1, node2, true)) return true;
    if (checkDistance(temp, node2, node3, true)) return true;
    if (checkDistance(temp, node3, node1, true)) return true;

    // 3 vertices of the unit convex cell vs an edge of the convex cell
    if (checkDistance(node1, temp, temp.next, false)) return true;
    if (checkDistance(node2, temp, temp.next, false)) return true;
    if (checkDistance(node3, temp, temp.next, false)) return true;
    temp = temp.next;
  }
  return false;
}

/**
 * This is a helper function of checkCollision() that checks if the distance between a
 * given vertex and a given line segment (two endpoints) is lower or equal to 9;
 */
boolean checkDistance(Node n1, Node n2, Node n3, boolean cOrU) {
  float distance;
  float x1 = (float) n1.xy[0];
  float y1 = (float) n1.xy[1];
  float x2 = (float) n2.xy[0];
  float y2 = (float) n2.xy[1];
  float x3 = (float) n3.xy[0];
  float y3 = (float) n3.xy[1];
  float v1x = x1 - x2;
  float v1y = y1 - y2;
  float v1Length = sqrt(v1x * v1x + v1y * v1y);
  float v2x = x3 - x2;
  float v2y = y3 - y2;
  float v2Length = sqrt(v2x * v2x + v2y * v2y);
  float dot = v1x * v2x + v1y * v2y;
  if (dot >= 0) {
    if (dot / v2Length > v2Length) distance = sqrt((x1 - x3) * (x1 - x3) + (y1 - y3) * (y1 - y3));
    else {
      float a = (y2 - y3) / (x2 - x3);
      float b = y2 - a * x2;
      distance = abs(a * x1 - y1 + b) / sqrt(a * a + (-1) * (-1));
    }
  } else distance = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

  // records two starting nodes for merging two hulls
  float v3Length = sqrt((x1 - x3) * (x1 - x3) + (y1 - y3) * (y1 - y3));
  if (cOrU) {
    startNodes[0] = n1;
    if (v1Length <= v3Length) startNodes[1] = n2;
    else startNodes[1] = n3;
  } else {
    startNodes[1] = n1;
    if (v1Length <= v3Length) startNodes[0] = n2;
    else startNodes[0] = n3;
  }

  if (distance <= 9) return true;
  else return false;
}

/**
 * This function merges the convex cell and the unit convex cell.
 * The algorithm is similar to the meging part of the divide and
 * conquer method, but it has to find out the starting pair of nodes
 * for merging first since they can be merge in any direction.
 */
void combineCells() {
  boolean check1 = true, check2 = true;
  Node temp1, temp2, temp3, temp4, temp5, temp6;
  Node[] nodeToBeConnected = new Node[4];

  // determinant
  int det;

  // coordinates to calculate determinant
  int x1, y1, x2, y2, x3, y3;
  temp1 = startNodes[1];
  temp2 = startNodes[0];
  temp3 = temp2.next;
  temp4 = startNodes[0];
  temp5 = startNodes[1];
  temp6 = temp5.previous;

  // a total of 4 sub-hulls should be checked while merging
  do {
    // first sub-hull
    while (check1) {
      x1 = temp1.xy[0];
      y1 = temp1.xy[1];
      x2 = temp2.xy[0];
      y2 = temp2.xy[1];
      x3 = temp3.xy[0];
      y3 = temp3.xy[1];
      det = x2 * y3 + x1 * y2 + y1 * x3 - x2 * y1 - x3 * y2 - y3 * x1;
      if (det >= 0) {
        check2 = true;
        temp4 = temp3;
        temp2 = temp3;
        temp3 = temp3.next;
      } else check1 = false;
    }

    // second sub-hull
    while (check2) {
      x1 = temp4.xy[0];
      y1 = temp4.xy[1];
      x2 = temp5.xy[0];
      y2 = temp5.xy[1];
      x3 = temp6.xy[0];
      y3 = temp6.xy[1];
      det = x2 * y3 + x1 * y2 + y1 * x3 - x2 * y1 - x3 * y2 - y3 * x1;
      if (det <= 0) {
        check1 = true;
        temp1 = temp6;
        temp5 = temp6;
        temp6 = temp6.previous;
      } else check2 = false;
    }
  } while (check1 || check2);

  // record
  nodeToBeConnected[0] = temp2;
  nodeToBeConnected[1] = temp5;

  check1 = true;
  check2 = true;
  temp1 = startNodes[1];
  temp2 = startNodes[0];
  temp3 = temp2.previous;
  temp4 = startNodes[0];
  temp5 = startNodes[1];
  temp6 = temp5.next;

  do {
    // third sub-hull
    while (check1) {
      x1 = temp1.xy[0];
      y1 = temp1.xy[1];
      x2 = temp2.xy[0];
      y2 = temp2.xy[1];
      x3 = temp3.xy[0];
      y3 = temp3.xy[1];
      det = x2 * y3 + x1 * y2 + y1 * x3 - x2 * y1 - x3 * y2 - y3 * x1;

      if (det <= 0) {
        check2 = true;
        temp4 = temp3;
        temp2 = temp3;
        temp3 = temp3.previous;
      } else check1 = false;
    }

    // fourth sub-hull
    while (check2) {
      x1 = temp4.xy[0];
      y1 = temp4.xy[1];
      x2 = temp5.xy[0];
      y2 = temp5.xy[1];
      x3 = temp6.xy[0];
      y3 = temp6.xy[1];
      det = x2 * y3 + x1 * y2 + y1 * x3 - x2 * y1 - x3 * y2 - y3 * x1;
      if (det >= 0) {
        check1 = true;
        temp1 = temp6;
        temp5 = temp6;
        temp6 = temp6.next;
      } else check2 = false;
    }
  } while (check1 || check2);
  nodeToBeConnected[2] = temp2;
  nodeToBeConnected[3] = temp5;
  
  // remove vertices do not belong to the convex cell.
  while (true) {
    if (nodeToBeConnected[2].next == nodeToBeConnected[0]) break;
    else convexCell.remove(nodeToBeConnected[2].next);
  }

  // add vertices belong to the convex cell
  while (true) {
    if (nodeToBeConnected[3] == nodeToBeConnected[1]) {
      convexCell.insert(nodeToBeConnected[2], nodeToBeConnected[3]);
      break;
    } else {
      Node temp = nodeToBeConnected[3].next;
      convexCell.insert(nodeToBeConnected[2], nodeToBeConnected[3]);
      nodeToBeConnected[2] = nodeToBeConnected[3];
      nodeToBeConnected[3] = temp;
    }
  }
  unitConvexCell.clear();
  unitCell = false;
}

/**
 * This function checks and returns if the convex cell has hit the wall. (collision detection between the convex cell and the walls)
 */
boolean checkWall() {
  Node temp = convexCell.getPointer();
  for (int i = 0; i < convexCell.size(); i++) {
    if (temp.xy[0] <= 8 || temp.xy[0] >= 490 || temp.xy[1] <= 8 || temp.xy[1] >= 490) return true;
    temp = temp.next;
  }
  return false;
}
