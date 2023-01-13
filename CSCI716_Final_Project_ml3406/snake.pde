/*
 * This file illustrates snake.pde (handles game Snake) from my final project.
 *
 * @author Michael Lee, ml3406@rit.edu
 */

/** the gap size between two consecutive cells of the snake body */ 
int gap;

/** the size of a single cell of the snake */
int bodySize;

/** spped of the snake (5: slowest; 0: fastest)*/
int speed;

/** a helper variable that counts the frames for regulating the snake speed */
int counter;

/** direction of the snake */
int direction;

/** a buffer that prevents the snake from changing its direction twice or more in a single frame */
int directionBuffer;

/** the x and y coordinates of the upper left grid */
int xStart, yStart;

/** if there is food on the screen */
boolean food;

/** a LinkedList to represent the snake */
LinkedList snake;

/** a 2D ArrayList to represents the game board for Snake */
ArrayList<ArrayList<Integer>> board;

/**
 * This function initializes all vaiables for the Snake game mode.
 */
void setupSnake() {
  gap = 1;
  bodySize = 9;
  speed = 5;
  counter = 0;
  direction = 0;
  directionBuffer = 0;
  xStart = 10;
  yStart = 10;
  food = false;
  board = new ArrayList();
  for (int i = 0; i < 48; i++) {
    board.add(new ArrayList());
    for (int j = 0; j < 48; j++) {
      board.get(i).add(0);
    }
  }
  board.get(25).set(24, 1);
  board.get(24).set(24, 1);
  board.get(23).set(24, 1);
  snake = new LinkedList();
  snake.addLast(new int[]{25, 24}, 0, 0, 0);
  snake.addLast(new int[]{24, 24}, 0, 0, 0);
  snake.addLast(new int[]{23, 24}, 0, 0, 0);
}

/**
 * This function handles the in game frames for Snake.
 */
void mainSnake() {
  if (!gameOver) {
    drawBodyAndFood();
    if (!food) {
      generateFood();
      food = true;
    } else if (counter < speed) {
      counter++;
    } else {
      counter = 0;
      resultNextMove(nextMove(snake.getFirst()));
      accelerate();
      directionBuffer++;
    }
  } else {
    setupSnake();
    page = 2;
  }
}

/**
 * This function detects keypress in Snake.
 */
void keySnake() {
  if (!gameOver) {
    if (keyCode == RIGHT && direction != 2 && directionBuffer >= 1) direction = 0;
    else if (keyCode == DOWN  && direction != 3 && directionBuffer >= 1) direction = 1;
    else if (keyCode == LEFT  && direction != 0 && directionBuffer >= 1) direction = 2;
    else if (keyCode == UP  && direction != 1 && directionBuffer >= 1) direction = 3;
    directionBuffer = 0;
  } else {
    if (keyCode == UP) restart = true;
    else if (keyCode == DOWN) restart = false;
    else if (keyCode == RIGHT && restart == false) gameMode = 0;
    else if (keyCode == RIGHT && restart == true) gameOver = false;
  }
}

/**
 * This function draws the snake and the food on the canvas.
 */
void drawBodyAndFood() {
  noStroke();
  for (int i = 0; i < 48; i++) {
    for (int j = 0; j < 48; j++) {
      if (board.get(i).get(j) == 1) {
        fill(255);
        rect(xStart + i * (bodySize + gap), yStart + j * (bodySize + gap), bodySize, bodySize);
      } else if (board.get(i).get(j) == 2) {
        fill(234, 234, 0);
        rect(xStart + i * (bodySize + gap), yStart + j * (bodySize + gap), bodySize, bodySize);
      }
    }
  }
}

/**
 * This function randomly generates a food at a valid position on the canvas.
 */
void generateFood() {
  int xTemp, yTemp;
  do {
    xTemp = (int) random(0, 47);
    yTemp = (int) random(0, 47);
    if (board.get(xTemp).get(yTemp) == 0) {
      board.get(xTemp).set(yTemp, 2);
      food = true;
    }
  } while (!food);
}

/**
 * This function determines the result of the next move accordingly.
 * food => snake gets longer
 * nothing => snake move forward
 * wall => game over
 */
void resultNextMove(int[] next) {
  // if wall
  if (next[0] > 47 || next[0] < 0 || next[1] > 47 || next[1] < 0 || board.get(next[0]).get(next[1]) == 1) gameOver = true;
  else {
    // if food
    if (board.get(next[0]).get(next[1]) == 2) {
      snake.addFirst(next, 0, 0, 0);
      board.get(next[0]).set(next[1], 1);
      food = false;
    } else {
      snake.addFirst(next, 0, 0, 0);
      board.get(next[0]).set(next[1], 1);
      int[] temp = snake.removeLast();
      board.get(temp[0]).set(temp[1], 0);
    }
  }
}

/**
 * This function performs accelerating according to the length of the snake.
 */
void accelerate() {
  if (snake.length >= 100) speed = 0;
  else if (snake.length >= 40) speed = 1;
  else if (snake.length >= 30) speed = 2;
  else if (snake.length >= 20) speed = 3;
  else if (snake.length >= 10) speed = 4;
}

/**
 * This function calculates and returns the position of the next move according to the direction of the snake.
 */
int[] nextMove(int[] head) {
  int[] temp = new int[2];
  switch (direction) {
  case 0:
    temp[0] = head[0] + 1;
    temp[1] = head[1];
    break;
  case 1:
    temp[0] = head[0];
    temp[1] = head[1] + 1;
    break;
  case 2:
    temp[0] = head[0] - 1;
    temp[1] = head[1];
    break;
  case 3:
    temp[0] = head[0];
    temp[1] = head[1] - 1;
    break;
  }
  return temp;
}
