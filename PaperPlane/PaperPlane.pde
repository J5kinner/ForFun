/*
 The Paper Plane Game
 CREATOR: Jonah Skinner
 ID:      44908415  
 **RULES**
 -> Move with Left and Right keys
 -> Dont touch the red boulders 
 -> Survive the waves of boulders and keep your high score for bragging rights
 -> Press any key to restart
 */
int UrandomInt( int upper ) { //required function
  upper = (int)random(0, upper);
  return upper;
}
int LrandomInt (int lower, int higher ) {
  lower = (int)random(lower, higher);
  return lower;
}
int xPosP = width/2; //Starting position of plane
int yPosP = height/68;
int speedX, speedY, NumPress, Points, highScore; 
int level = 11; 
int Bsize = 100; //Boulder size
float[] xposB;//Boulder positions
float[] yposB;
void setup() { 
  size(512, 768);
  xposB = new float[60];
  yposB = new float[60];
} 
void draw() { // Background drawn, shape colour and outline chosen
  brickWall();
  smooth();
  reset();
  fill(255);
  textSize(width/16);
  text(Points, width/12, height/20);
  Plane();
  Boulder(level);
  numBoulder(level);
}
void reset() {
  if (yPosP>=height) {
    yPosP=0;
    level = UrandomInt(11);
    numBoulder(level);
    Points++;
    highScore++;
  }
}
void numBoulder(int B) { //B = Number of boulders
  if (B>=10) {
    for (int i=0; i<level; i++) {
      xposB [i]= UrandomInt(width);
      yposB [i]= LrandomInt(height/4, height);
    }
    level = UrandomInt(11);
  }
  for (int i =0; i < level; i++) { //Touch boulder = game over
    if (dist(xposB[i], yposB[i], xPosP, yPosP) <= Bsize/2) {
      DEADplane();
    }
  }
  Boulder(level);
}
void Boulder(int B) {
  fill(#FFFFFF);
  stroke(70, 90, 197);
  for (int i =0; i < B; i++) {
    ellipse(xposB[i], yposB[i], Bsize, Bsize);
  }
}
void brickWall() {
  background(#551F06);
  stroke(#957469);
  for (int i = 0; i <55; i++) { //Horizontal lines of the brick wall
    line(0, i *15, width, i*15);
  }
  for (int i = 0; i<15; i++) {// The end points of the bricks
    for (int j = 0; j<30; j++) {
      line(35/2+i*35, 15+j*30, 35/2+i*35, (j+1)*30);
      line(i*35, j*30, i*35, 15+(j*30));
    }
  }
}
void keyPressed() { //The keyboard Control Panel 
  if (keyCode != RIGHT && keyCode != LEFT) {
    xPosP = width/2;
    yPosP = 10;
    NumPress = 0;
    level = UrandomInt(11);
    numBoulder(level);
  }
  if (keyCode == RIGHT) {
    NumPress++;
  } 
  if (keyCode == LEFT) {
    NumPress--;
  }
}
void Plane() { //How processing interprets controls
  fill(255);
  stroke(0);
  xPosP+=speedX;
  yPosP+=speedY;
  if (NumPress == 0)
    Splane(xPosP, yPosP);
  if (NumPress == -1)
    SWplane(xPosP, yPosP);
  if (NumPress == 1)
    SEplane(xPosP, yPosP);
  if (NumPress == 2)
    Eplane(xPosP, yPosP);
  if (NumPress ==-2)
    Wplane(xPosP, yPosP);
  if (NumPress > 2)
    NumPress = 2;
  if (NumPress< -2)
    NumPress = -2;
}
void DEADplane() { 
  brickWall();
  stroke(0);
  fill(#00FF86);
  triangle(xPosP-5, yPosP-5, xPosP, yPosP-5, xPosP-4, yPosP);
  triangle(xPosP+12, yPosP-8, xPosP+16, yPosP-4, xPosP+12, yPosP -4);
  triangle(xPosP+8, yPosP+8, xPosP+10, yPosP+14, xPosP+6, yPosP+12);
  triangle(xPosP-8, yPosP-10, xPosP-16, yPosP-4, xPosP-8, yPosP+4);
  speedX = 0;
  speedY = 0;
  fill(255);
  textAlign(CENTER);
  textSize(width/6);
  text("GAME OVER", width/2, height/8);
  textSize(width/20);
   text("PRESS ANY KEY TO PLAY", width/2, height/4.5);
  text(highScore, width/2+width/6, height/5.5);
  if (highScore !=0){
    text("Good Job!", width/2+ width/3, height/5.5);
  }
  fill(#F9FA38);
  text("High Score", width/2, height/5.5);
}
void Splane(int x, int y) {   //South moving plane (down the screen = South)
  triangle(x-10, y-10, x+10, y-10, x, y+10);
  speedX = 0;
  speedY = 5;
}
void SWplane(int x, int y) {  //South west plane (LEFT AT 45 degrees from south)
  triangle(x, y-10, x+10, y+2, x-8, y+8);
  speedX = -2;
  speedY =  3;
}
void Wplane(int x, int y) {   //West plane (LEFT at 180 degrees)
  triangle(x+10, y-10, x+10, y+10, x-10, y);
  speedX = -4;
  speedY =  1;
}
void Eplane(int x, int y) {   //East plane (RIGHT at 180 degrees)
  triangle(x-10, y-10, x-10, y+10, x+10, y);
  speedX = 4;
  speedY = 1;
}
void SEplane(int x, int y) {  //South East plane (RIGHT at 45 degrees from south)
  triangle(x, y-10, x-10, y+4, x+8, y+8);
  speedX = 2;
  speedY = 3;
}
