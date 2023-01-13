/*
 * This file illustrates cell.pde (handles game Cell) from my final project.
 *
 * @author Michael Lee, ml3406@rit.edu
 */

/** the diameter of the cell */
int diameter;

/** x/y coordinates of the cell */
int xCell, yCell;

/** x/y coordinates of the food */
int xFood, yFood;

/** if there is a cell food on the screen or not */
boolean cellFood;

/**
 * This function initializes all vaiables for the Cell game mode.
 */
void setupCell() {
  diameter = 30;
  xCell = 250;
  yCell = 250;
  xFood = 0;
  yFood = 0;
  cellFood = false;
  generateCell();
}

/**
 * This function handles the in game frames for Cell.
 */
void mainCell() {
  if (!gameOver) {
    if (!cellFood) generateCell();
    moveCell();
    drawCell();
    if (checkEat()) {
      diameter += 3;
      cellFood = false;
    }
    if (checkDie()) gameOver = true;
  } else {
    setupCell();
    page = 2;
  }
}

/**
 * This function detects keypress in Cell.
 */
void keyCell() {
  if (gameOver) {
    if (keyCode == UP) restart = true;
    else if (keyCode == DOWN) restart = false;
    else if (keyCode == RIGHT && restart == false) gameMode = 0;
    else if (keyCode == RIGHT && restart == true) gameOver = false;
  }
}

/**
 * This function generates a cell food at a random but valid position on the canvas.
 */
void generateCell() {
  int temp;
  int counter = 0;
  do {
    if (counter >= 30000) {
      gameOver = true;
      break;
    }
    xFood = (int) random(38, 461);
    yFood = (int) random(38, 461);
    temp = (xCell - xFood) * (xCell - xFood) + (yCell - yFood) * (yCell - yFood);
    counter++;
  } while (temp <= (5 + diameter / 2.0) * (5 + diameter / 2.0));
  cellFood = true;
}

/**
 * This function move the position of the cell toward the current mouse position on the canvas.
 */
void moveCell() {
  float distance = sqrt((mouseX - xCell) * (mouseX - xCell) + (mouseY - yCell) * (mouseY - yCell));
  if (distance >= 3) {
    if (mousePressed) {
      xCell += (int) (4.5 * (mouseX - xCell) / distance);
      yCell += (int) (4.5 * (mouseY - yCell) / distance);
    } else {
      xCell += (int) (3 * (mouseX - xCell) / distance);
      yCell += (int) (3 * (mouseY - yCell) / distance);
    }
  }
}

/**
 * This function draws the cell and the food on the canvas per frame.
 */
void drawCell() {
  // cell
  fill(255);
  noStroke();
  ellipse(xCell, yCell, diameter, diameter);
  
  // food
  fill(234, 234, 0);
  ellipse(xFood, yFood, 10, 10);
}

/**
 * This function checks and return if the cell is close enough to eat the food. (collision detection between cell and food)
 */
boolean checkEat() {
  if ((xCell - xFood) * (xCell - xFood) + (yCell - yFood) * (yCell - yFood) <= (5 + diameter / 2) * (5 + diameter / 2)) return true;
  else return false;
}

/**
 * This function checks and returns if the cell is dead or not. (collision detection between cell and walls)
 */
boolean checkDie() {
  if (xCell - diameter / 2 <= 8 || xCell + diameter / 2 >= 490 || yCell - diameter / 2 <= 8 || yCell + diameter / 2 >= 490) return true;
  return false;
}
