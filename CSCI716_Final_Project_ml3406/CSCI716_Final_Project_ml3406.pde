/*
 * This file illustrates CSCI716_Fianl_Project_ml3406.pde (main code) from my final project.
 *
 * @author Michael Lee, ml3406@rit.edu
 */

/** page decides what background should be drawn */
int page;

/** gameMode decides what game to play */
int gameMode;

/** category decides to play pedagogical games or the original games */
boolean category;

/** restart decides to restart the game or not while the game is over */
boolean restart;

/** gameOver tells the program whether the game is over */
boolean gameOver;

/** to refresh balck background or not */
boolean drawBalckBackground;

/**
 * This function is executed once at the beginning to initialize some important variables.
 */
void setup() {
  // define the canvas size
  size(499, 499);
  page = 0;
  gameMode = 0;
  category = true;
  gameOver = false;
  restart = true;
  drawBalckBackground = true;

  // initializes all variables for every game mode
  setupSnake();
  setupVoronoiSnake();
  setupCell();
  setupConvexCell();
}

/**
 * This function is executed repeatedly (every frame) for animation.
 */
void draw() {
  // 0: homepage; 1: game mode page; 2: gameover page; 3: in game
  bg(page);
  if (page == 3) {
    switch(gameMode) {
    case 0:
      if (category) mainVoronoiSnake();
      else mainSnake();
      break;
    case 1:
      if (category) mainConvexCell();
      else mainCell();
      break;
    }
  }
}


/**
 * This function detects keypress while the program is executed.
 */
void keyPressed() {
  if (key == CODED) {
    switch (page) {
    case 0:
      if (keyCode == UP) category = true;
      else if (keyCode == DOWN)  category = false;
      else if (keyCode == RIGHT) {
        page = 1;
        gameMode = 0;
      }
      break;
    case 1:
      if (keyCode == UP && gameMode != 0) gameMode--;
      else if (keyCode == DOWN && gameMode != 1)  gameMode++;
      else if (keyCode == RIGHT) page = 3;
      else if (keyCode == LEFT) page = 0;
      break;
    case 2:
      if (keyCode == UP) restart = true;
      else if (keyCode == DOWN) restart = false;
      else if (keyCode == RIGHT && restart) {
        gameOver = false;
        page = 3;
      } else if (keyCode == RIGHT && !restart) {
        category = true;
        gameOver = false;
        restart = true;
        page = 0;
      }
      break;
    case 3:
      switch(gameMode) {
      case 0:
        if (category) keyVoronoiSnake();
        else keySnake();
        break;
      case 1:
        if (category) keyConvexCell();
        else keyCell();
        break;
      }
      break;
    }
  }
}

/**
 * This function draws background accordingly.
 */
void bg(int choice) {
  if (drawBalckBackground) background(0);
  stroke(255, 0, 0);
  strokeWeight(9);
  noFill();
  rect(4, 4, 490, 490);
  switch (choice) {
  case 0: // title page
    if (category) {
      textSize(36);
      fill(0, 255, 0);
      text("CSCI 716 Games →", 99, 210);
      fill(255);
      text("Original Games →", 99, 300);
    } else {
      textSize(36);
      fill(255);
      text("CSCI 716 Games →", 99, 210);
      fill(0, 255, 0);
      text("Original Games →", 99, 300);
    }
    break;
  case 1: // choose game
    textSize(36);
    if (category) {
      if (gameMode == 0) fill(0, 255, 0);
      else fill(255);
      text("Voronoi Snake →", 99, 210);
      if (gameMode == 1) fill(0, 255, 0);
      else fill(255);
      text("Convex Cell →", 99, 300);
    } else {
      if (gameMode == 0) fill(0, 255, 0);
      else fill(255);
      text("Snake →", 99, 210);
      if (gameMode == 1) fill(0, 255, 0);
      else fill(255);
      text("Cell →", 99, 300);
    }
    break;
  case 2: // game over
    fill(255);
    textSize(72);
    text("Game Over !", 60, 210);
    textSize(60);
    text("Restart ?", 60, 300);
    textSize(36);
    if (restart) {
      fill(0, 255, 0);
      text("YES →", 345, 270);
      fill(255);
      text("NO", 345, 330);
    } else {
      text("YES", 345, 270);
      fill(255, 0, 0);
      text("NO →", 345, 330);
    }
    break;
  case 3:
    break;
  }
}
