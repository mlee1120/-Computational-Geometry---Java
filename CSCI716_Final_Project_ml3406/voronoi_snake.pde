/** //<>//
 * This file illustrates voronoi_snake.pde (handles game Voronoi Snake) from my final project.
 *
 * @author Michael Lee, ml3406@rit.edu
 */

/** the gap size between two consecutive cells of the Voronoi snake body */
int gapV;

/** the size of a single cell of the Voronoi snake */
int bodySizeV;

/** spped of the Voronoi snake (5: slowest; 0: fastest)*/
int speedV;

/** a helper variable that counts the frames for regulating the Voronoi snake speed */
int counterV;

/** direction of the Voronoi snake */
int directionV;

/** a buffer that prevents the snake from changing its direction twice or more in a single frame */
int directionBufferV;

/** the x and y coordinates of the upper left grid */
int xStartV, yStartV;

/** if there is food on the screen */
boolean foodV;

/** node of the food (position and color) */
Node foodNode;

/** to draw Voronoi diagram or not */
boolean voronoi;

/** if the diagram is displayed or not (for asking to restart or not) */
boolean diagram;

/** a helper frame coounter for the final diagram display */
int frameV;

/** a LinkedList to represent the Voronoi snake */
LinkedList snakeV;

/** a 2D ArrayList to represents the game board for Voronoi Snake */
ArrayList<ArrayList<Integer>> boardV;

/** a 2D ArrayList that stores every pixel's site */
ArrayList<ArrayList<Node>> voronoiDiagram;



/**
 * This function initializes all vaiables for the Voronoi Snake game mode.
 */
void setupVoronoiSnake() {
  gapV = 1;
  bodySizeV = 29;
  speedV = 6;
  counterV = 0;
  directionV = 0;
  directionBufferV = 0;
  xStartV = 10;
  yStartV = 10;
  foodV = false;
  voronoi = false;
  boardV = new ArrayList();
  for (int i = 0; i < 16; i++) {
    boardV.add(new ArrayList());
    for (int j = 0; j < 16; j++) {
      boardV.get(i).add(0);
    }
  }
  boardV.get(9).set(8, 1);
  boardV.get(8).set(8, 1);
  boardV.get(7).set(8, 1);
  snakeV = new LinkedList();
  snakeV.addLast(new int[]{9, 8}, (int) random(18, 255), (int) random(18, 255), (int) random(18, 255));
  snakeV.addLast(new int[]{8, 8}, (int) random(18, 255), (int) random(18, 255), (int) random(18, 255));
  snakeV.addLast(new int[]{7, 8}, (int) random(18, 255), (int) random(18, 255), (int) random(18, 255));
  voronoiDiagram = new ArrayList();
  diagram = false;
  frameV = 0;
}

/**
 * This function handles the in game frames for Voronoi Snake.
 */
void mainVoronoiSnake() {
  if (!gameOver) {
    if (!voronoi) {
      drawBodyAndFoodV();
      if (!foodV) {
        generateFoodV();
        foodV = true;
      } else if (counterV < speedV) {
        counterV++;
      } else {
        counterV = 0;
        resultNextMoveV(nextMoveV(snakeV.getFirst()));
        accelerateV();
        directionBufferV++;
      }
    } else {
      if (drawBalckBackground) {
        drawVoronoi();
        drawBalckBackground = false;
      }
      if (frameV == 60) diagram = true;
      if (frameV < 60) frameV++;
    }
  } else {
    setupVoronoiSnake();
    page = 2;
  }
}

/**
 * This function detects keypress in Voronoi Snake.
 */
void keyVoronoiSnake() {
  if (!gameOver) {
    if (keyCode == RIGHT && directionV != 2 && directionBufferV >= 1) directionV = 0;
    else if (keyCode == DOWN  && directionV != 3 && directionBufferV >= 1) directionV = 1;
    else if (keyCode == LEFT  && directionV != 0 && directionBufferV >= 1) directionV = 2;
    else if (keyCode == UP  && directionV != 1 && directionBufferV >= 1) directionV = 3;
    if (diagram && (keyCode == RIGHT || keyCode == LEFT || keyCode == UP || keyCode == DOWN)) {
      gameOver = true;
      drawBalckBackground = true;
    }
    directionBufferV = 0;
  } else {
    if (keyCode == UP) restart = true;
    else if (keyCode == DOWN) restart = false;
    else if (keyCode == RIGHT && restart == false) gameMode = 0;
    else if (keyCode == RIGHT && restart == true) gameOver = false;
  }
}

/**
 * This function draws the Voronoi snake and the food on the canvas.
 */
void drawBodyAndFoodV() {
  noStroke();
  if (foodV) {
    fill(foodNode.rgb[0], foodNode.rgb[1], foodNode.rgb[2]);
    ellipse(xStartV + foodNode.xy[0] * (bodySizeV + gapV) + 14, yStartV + foodNode.xy[1] * (bodySizeV + gapV) + 14, bodySizeV * 1.5, bodySizeV * 1.5);
  }
  Node pointer = snakeV.getPointer();
  for (int i = 0; i < snakeV.size(); i++) {
    fill(pointer.rgb[0], pointer.rgb[1], pointer.rgb[2]);
    ellipse(xStartV + pointer.xy[0] * (bodySizeV + gapV) + 14, yStartV + pointer.xy[1] * (bodySizeV + gapV) + 14, bodySizeV * 1.5, bodySizeV * 1.5);
    pointer = pointer.next;
  }
  pointer = snakeV.getPointer();
  for (int i = 0; i < snakeV.size(); i++) {
    fill(pointer.rgb[0], pointer.rgb[1], pointer.rgb[2]);
    rect(xStartV + pointer.xy[0] * (bodySizeV + gapV), yStartV + pointer.xy[1] * (bodySizeV + gapV), bodySizeV, bodySizeV);
    fill(0, 0, 0);
    ellipse(xStartV + pointer.xy[0] * (bodySizeV + gapV) + 14, yStartV + pointer.xy[1] * (bodySizeV + gapV) + 14, 3, 3);
    pointer = pointer.next;
  }
  if (foodV) {
    fill(foodNode.rgb[0], foodNode.rgb[1], foodNode.rgb[2]);
    rect(xStartV + foodNode.xy[0] * (bodySizeV + gapV), yStartV + foodNode.xy[1] * (bodySizeV + gapV), bodySizeV, bodySizeV);
    fill(0, 0, 0);
    ellipse(xStartV + foodNode.xy[0] * (bodySizeV + gapV) + 14, yStartV + foodNode.xy[1] * (bodySizeV + gapV) + 14, 3, 3);
  }
  stroke(255, 0, 0);
  strokeWeight(9);
  noFill();
  rect(4, 4, 490, 490);
}

/**
 * This function randomly generates a food at a valid position on the canvas.
 */
void generateFoodV() {
  int xTemp, yTemp;
  do {
    xTemp = (int) random(0, 15);
    yTemp = (int) random(0, 15);
    if (boardV.get(xTemp).get(yTemp) == 0) {
      boardV.get(xTemp).set(yTemp, 2);
      foodNode = new Node(new int[]{xTemp, yTemp}, (int) random(18, 255), (int) random(18, 255), (int) random(18, 255));
      foodV = true;
    }
  }
  while (!foodV);
}

/**
 * This function determines the result of the next move accordingly.
 * food => Voronoi snake gets longer
 * nothing => Voronoi snake move forward
 * wall => game over
 */
void resultNextMoveV(int[] next) {
  // if wall
  if (next[0] > 15 || next[0] < 0 || next[1] > 15 || next[1] < 0 || boardV.get(next[0]).get(next[1]) == 1) voronoi = true;
  else {
    // if food
    if (boardV.get(next[0]).get(next[1]) == 2) {
      snakeV.addFirst(next, foodNode.rgb[0], foodNode.rgb[1], foodNode.rgb[2]);
      boardV.get(next[0]).set(next[1], 1);
      foodV = false;
    } else {
      snakeV.addFirst(next, 0, 0, 0);
      boardV.get(next[0]).set(next[1], 1);
      Node pointer = snakeV.getPointer();
      for (int i = 0; i < snakeV.size() - 1; i++) {
        pointer.rgb = pointer.next.rgb;
        pointer = pointer.next;
      }
      int[] temp = snakeV.removeLast();
      boardV.get(temp[0]).set(temp[1], 0);
    }
  }
}

/**
 * This function performs accelerating according to the length of the snake.
 */
void accelerateV() {
  if (snakeV.length >= 100) speedV = 1;
  else if (snakeV.length >= 40) speedV = 2;
  else if (snakeV.length >= 30) speedV = 3;
  else if (snakeV.length >= 20) speedV = 4;
  else if (snakeV.length >= 10) speedV = 5;
}

/**
 * This function calculates and returns the position of the next move according to the direction of the snake.
 *
 * @param head the x/y coordinates of the Voronoi snake head
 * @return the x/y coordinates of the next move
 */
int[] nextMoveV(int[] head) {
  int[] temp = new int[2];
  switch (directionV) {
  case 0: // right
    temp[0] = head[0] + 1;
    temp[1] = head[1];
    break;
  case 1: // down
    temp[0] = head[0];
    temp[1] = head[1] + 1;
    break;
  case 2: // left
    temp[0] = head[0] - 1;
    temp[1] = head[1];
    break;
  case 3: // up
    temp[0] = head[0];
    temp[1] = head[1] - 1;
    break;
  }
  return temp;
}

/**
 * This function computes the Voronoi diagram by iterating through all pixels and assigns
 * them to their own nearest site. After that, it draws the Voronoi diagram on the canvas.
 */
void drawVoronoi() {
  // a pointer used to iterate through the Voronoi snake
  Node pointer = snakeV.getPointer();

  //currently assigned site
  Node current;
  int distanceSquare1, distanceSquare2;

  // x/y coordinates of the current site
  int x0, y0;

  // x/y coordinates of the pixel's currently assigned site
  int x1, y1;

  // whether to keep scaning or not for incremental algorithm
  boolean carryOn1, carryOn2, carryOn3;

  // assign every pixel to its closest site
  for (int i = 0; i< snakeV.size(); i++) {
    if (i == 0) {
      for (int j = 0; j < 481; j++) {
        voronoiDiagram.add(new ArrayList());
        for (int k = 0; k < 481; k++) {
          voronoiDiagram.get(j).add(pointer);
        }
      }
    } else {
      x0 = xStartV + pointer.xy[0] * (bodySizeV + gapV) + 14;
      y0 = yStartV + pointer.xy[1] * (bodySizeV + gapV) + 14;

      // right side of the site
      int j = x0;
      carryOn1 = true;
      carryOn2 = true;
      while (carryOn1 || carryOn2) {
        // lower right
        int k = y0;
        carryOn3 = false;
        while (carryOn1) {
          current = voronoiDiagram.get(j).get(k);
          x1 = xStartV + current.xy[0] * (bodySizeV + gapV) + 14;
          y1 = yStartV + current.xy[1] * (bodySizeV + gapV) + 14;
          distanceSquare1 = (x1 - 9 - j) * (x1 - 9 - j) + (y1 - 9 - k) * (y1 - 9 - k);
          distanceSquare2 = (x0 - 9 - j) * (x0 - 9 - j) + (y0 - 9 - k) * (y0 - 9 - k);
          if (distanceSquare1 > distanceSquare2) {
            voronoiDiagram.get(j).set(k, pointer);
            carryOn3 = true;
          } else if (carryOn3) break;
          k++;
          if (k == 481 && !carryOn3) carryOn1 = false;
          if (k == 481) break;
        }
        
        // upper right
        k = y0 - 1;
        carryOn3 = false;
        while (carryOn2) {
          current = voronoiDiagram.get(j).get(k);
          x1 = xStartV + current.xy[0] * (bodySizeV + gapV) + 14;
          y1 = yStartV + current.xy[1] * (bodySizeV + gapV) + 14;
          distanceSquare1 = (x1 - 9 - j) * (x1 - 9 - j) + (y1 - 9 - k) * (y1 - 9 - k);
          distanceSquare2 = (x0 - 9 - j) * (x0 - 9 - j) + (y0 - 9 - k) * (y0 - 9 - k);
          if (distanceSquare1 > distanceSquare2) {
            voronoiDiagram.get(j).set(k, pointer);
            carryOn3 = true;
          } else if (carryOn3) break;
          k--;
          if (k == -1 && !carryOn3) carryOn2 = false;
          if (k == -1) break;
        }
        j++;
        if (j == 481) {
          carryOn1 = false;
          carryOn2 = false;
        }
      }

      // left side of the site
      j = x0 - 1;
      carryOn1 = true;
      carryOn2 = true;
      while (carryOn1 || carryOn2) {
        // lower left
        int k = y0;
        carryOn3 = false;
        while (carryOn1) {
          current = voronoiDiagram.get(j).get(k);
          x1 = xStartV + current.xy[0] * (bodySizeV + gapV) + 14;
          y1 = yStartV + current.xy[1] * (bodySizeV + gapV) + 14;
          distanceSquare1 = (x1 - 9 - j) * (x1 - 9 - j) + (y1 - 9 - k) * (y1 - 9 - k);
          distanceSquare2 = (x0 - 9 - j) * (x0 - 9 - j) + (y0 - 9 - k) * (y0 - 9 - k);
          if (distanceSquare1 > distanceSquare2) {
            voronoiDiagram.get(j).set(k, pointer);
            carryOn3 = true;
          } else if (carryOn3) break;
          k++;
          if (k == 481 && !carryOn3) carryOn1 = false;
          if (k == 481) break;
        }
        
        // upper left
        k = y0 - 1;
        carryOn3 = false;
        while (carryOn2) {
          current = voronoiDiagram.get(j).get(k);
          x1 = xStartV + current.xy[0] * (bodySizeV + gapV) + 14;
          y1 = yStartV + current.xy[1] * (bodySizeV + gapV) + 14;
          distanceSquare1 = (x1 - 9 - j) * (x1 - 9 - j) + (y1 - 9 - k) * (y1 - 9 - k);
          distanceSquare2 = (x0 - 9 - j) * (x0 - 9 - j) + (y0 - 9 - k) * (y0 - 9 - k);
          if (distanceSquare1 > distanceSquare2) {
            voronoiDiagram.get(j).set(k, pointer);
            carryOn3 = true;
          } else if (carryOn3) break;
          k--;
          if (k == -1 && !carryOn3) carryOn2 = false;
          if (k == -1) break;
        }
        j--;
        if (j == 0) {
          carryOn1 = false;
          carryOn2 = false;
        }
      }
    }
    pointer = pointer.next;
  }

  // draw Voronoi diagram
  for (int i = 0; i < 481; i++) {
    for (int j = 0; j < 481; j++) {
      set(i + 9, j + 9, color(voronoiDiagram.get(i).get(j).rgb[0], voronoiDiagram.get(i).get(j).rgb[1], voronoiDiagram.get(i).get(j).rgb[2]));
    }
  }

  // draw sites
  noStroke();
  fill(0);
  pointer = snakeV.getPointer();
  for (int i = 0; i < snakeV.size(); i++) {
    ellipse(xStartV + pointer.xy[0] * (bodySizeV + gapV) + 14, yStartV + pointer.xy[1] * (bodySizeV + gapV) + 14, 3, 3);
    pointer = pointer.next;
  }
}
