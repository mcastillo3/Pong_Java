package classProjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Pong {
	//set the window of the game
	static int GAME_WIDTH = 1000;
	static int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));
	//main driver
	public static void main (String[] args) {
		PongFrame f = new PongFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(GAME_WIDTH, GAME_HEIGHT);
		f.setVisible(true);
		f.setup();
		f.run();
	}
}

@SuppressWarnings("serial")
class PongFrame extends JFrame  implements KeyListener {
	//set variables and objects
	int GAME_WIDTH = 1000;
	int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));
	HumanPaddle p1;
	AIPaddle p2;
	Score score;
	MultiBall b1;
	ArrayList<MultiBall> multiballs = new ArrayList<MultiBall>();	//create a list of multiple balls
	ArrayList<Blocks> blocks = new ArrayList<Blocks>();				//create a list of multiple blocks
	static private int ballNumber;
	boolean gameStart;
	private Image raster;
	private Graphics rasterGraphics;
	int time = 10;
	long runTime = 0;
	//set the frame of the game and instantiate objects
	public PongFrame() {
		this.setResizable(false);
		this.pack();
		this.addKeyListener(this);
		p1 = new HumanPaddle(1);
		addBall();
		addBlocks();
		p2 = new AIPaddle(2, multiballs.get(0));
		score = new Score(GAME_WIDTH, GAME_HEIGHT);
		b1 = new MultiBall();
		gameStart = false;
	}
	//initial setup and create a copies of images
	public void setup() {
		raster = this.createImage(GAME_WIDTH, GAME_HEIGHT);
		rasterGraphics = raster.getGraphics();
	}
	//main engine to run the game
	public void run() {
		rasterGraphics.setColor(Color.black);
		rasterGraphics.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

		while (true) {
			runTime += time;		//keep track of runtime to eventually add new balls at intervals
			paint(rasterGraphics);
			//if either player scores 3 points, game is over and variables are reset
			//program still running in case player wants to play again
			if (score.getPlayer1Score() == 3 || score.getPlayer2Score() == 3) {
				for (MultiBall b : multiballs) {
					b.Velocity.set(0, 0);
					b.position.set(0, 0);
				}
				p1.setY(GAME_HEIGHT/2 - 50);
				p2.setY(GAME_HEIGHT/2 - 50);
				//display Game Over
				rasterGraphics.setColor(Color.red);
				rasterGraphics.setFont(new Font("Consolas", Font.BOLD, 40));
				rasterGraphics.drawString("Game Over", ((GAME_WIDTH/2)-103), (GAME_HEIGHT / 2)-97);
				//check which player won and display appropriate message
				if (score.getPlayer1Score() == 3) {
					rasterGraphics.setFont(new Font("Consolas", Font.BOLD, 30));
					rasterGraphics.setColor(Color.blue);
					rasterGraphics.drawString("You Win!", ((GAME_WIDTH/2)-55), (GAME_HEIGHT / 2)-30);
					rasterGraphics.setFont(new Font("Consolas", Font.PLAIN, 20));
					rasterGraphics.setColor(Color.white);
					rasterGraphics.drawString("Press Space to Play Again", ((GAME_WIDTH/2)-122), (GAME_HEIGHT / 2));
				}
				else if (score.getPlayer2Score() == 3) {
					rasterGraphics.setFont(new Font("Consolas", Font.BOLD, 30));
					rasterGraphics.setColor(Color.red);
					rasterGraphics.drawString("You Lose!", ((GAME_WIDTH/2)-58), (GAME_HEIGHT / 2)-30);
					rasterGraphics.setFont(new Font("Consolas", Font.PLAIN, 20));
					rasterGraphics.setColor(Color.white);
					rasterGraphics.drawString("Press Space to Play Again", ((GAME_WIDTH/2)-122), (GAME_HEIGHT / 2));
				}
			}
			//waits for player to start the game, then the main engine starts running
			if(gameStart) {
				p1.move();
				for (MultiBall b : multiballs) {	//iterate through the ball list and moves each ball in the array
				b.move(multiballs);
				p2.move(b);							//move the AI player accordingly to each ball
				b.draw(rasterGraphics);
				b.checkPaddleCollision(p1, p2);		//check if the ball collides with either paddle
					//minimize the area to check if the ball collides with any of the bricks
					if(b.position.getX() < GAME_WIDTH*.20 || b.position.getX() > GAME_WIDTH*.80) {
						for (Blocks bl : blocks) {	//iterate through the blocks list and checks if the 
													//current ball in the array collides with the brick
							if(bl.checkBlockCollision(b, bl)) {
								bl.setIsDestroyed(true);	//if the ball and brick collide, set the block to "destroyed"
									if(b.position.getX() < GAME_WIDTH/2)	//check which side of the game screen the ball is on
										score.player2Score();				//to assign the player point accordingly
									else
										score.player1Score();
							}
						}
					}
				}
			}
			//when the game is first started, the screen pauses, displays the title of the game and waits for player to hit Enter
			if(!gameStart) {
				rasterGraphics.setColor(Color.white);
				rasterGraphics.setFont(new Font("Consolas", Font.PLAIN, 20));
				rasterGraphics.drawString("BREAK THOSE BRICKS!", ((GAME_WIDTH/2)-90), (GAME_HEIGHT / 2)-97);
				rasterGraphics.drawString("Press Enter to Begin", ((GAME_WIDTH/2)-100), (GAME_HEIGHT / 2)-77);
			}
			
			getGraphics().drawImage(raster,0,0,GAME_WIDTH,GAME_HEIGHT,null);
			try {
				Thread.sleep(time);		//sleeps the game for an amount of time
			} catch (InterruptedException e) {}
			
			if (runTime >= 10000) {		//after 15 seconds of runtime, add a new ball to the game
			addBall();
			runTime = 0;				//reset the runtime
			}
		}
		
	}
	//create a new ball into the arraylist with a random color
	public void addBall() {
		double hue = Math.random();
		int rgb = Color.HSBtoRGB((float) hue, 0.5f, 0.5f);
		Color color = new Color(rgb);
		multiballs.add(new MultiBall(ballNumber, color));
		ballNumber++;
	}
	//create a new brick into the arraylist
	public void addBlocks() {
		int side = 0;
		for(int i = 0; i < 12; i++) {
			if(i < 6)			//variable to check if the brick will be on the left or right side
				blocks.add(new Blocks (i, side));
			if(i >= 6) {
				side = 1;
				blocks.add(new Blocks (i, side));
			}
		}
	}
	//drawing engine of all elements
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		
		p1.draw(g);
		p2.draw(g);
		for(Blocks bl: blocks) {	//draws a brick only if it hasn't been destroyed
			if(bl != null && !bl.isDestroyed())
				bl.drawBlock(g);
			else
				bl = null;
		}
		for(MultiBall b : multiballs) {
			b.draw(g);
		}
		score.draw(g);
	}
	
	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_NUMPAD5) {	//uses keypad up to move human player
			p1.setUpAccel(true);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2) { //uses keypad down to move human player
			p1.setDownAccel(true);
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER) { //uses keypad enter to start the game
			gameStart = true;
		}
		else if (e.getKeyCode() == KeyEvent.VK_SPACE) { //uses keypad space to reset and repaint the game
			score.setPlayer1Score(0);
			score.setPlayer2Score(0);
			multiballs.clear();
			for(Blocks bl: blocks) {
				bl.setIsDestroyed(false);
			}
			repaint();
			runTime=9750;
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_NUMPAD5) {		//if player releases keypad up, up acceleration is slowed
			p1.setUpAccel(false);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2) { //if player releases keypad down, down acceleration is slowed
			p1.setDownAccel(false);
		}
	}
}
//interface for the human and AI paddles
interface Paddle {
	static int GAME_WIDTH = 1000;
	static int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));
	static int PADDLE_WIDTH = 25;
	static int PADDLE_HEIGHT = 100;
	static int PADDLE_BUFFER = 50;
	static int BLOCK_HEIGHT = 100;
	static int BLOCK_WIDTH = 25;
	final double FRICTION = 0.90;
	
	public void draw(Graphics g);
	public void move();
	public int getY();
	public Rectangle getBoundingBox();
}

class HumanPaddle implements Paddle {
	double y, yVel;
	boolean upAccel, downAccel;
	int player, x;
	Rectangle boundingBox;
	
	public HumanPaddle(int player) {	//construct the human player paddle
		upAccel = false;				//start with no movement
		downAccel = false;
		y = GAME_HEIGHT/2 - PADDLE_HEIGHT/2;	//start at middle of the screen
		yVel = 0;
		x = PADDLE_WIDTH + BLOCK_WIDTH;			//start at left side of screen
		
		boundingBox = new Rectangle(x,(int)y, PADDLE_WIDTH+8, PADDLE_HEIGHT); //create a perimeter around the paddle
		boundingBox.setBounds(boundingBox);
	}

	public void draw(Graphics g) {	//draw method for the paddles features
		Graphics2D g2 = (Graphics2D) g;
		int thickness = 2;
		g2.setStroke(new BasicStroke(thickness));
		g.setColor(Color.white);
		g.drawRect(x, (int) y, PADDLE_WIDTH, PADDLE_HEIGHT);

		g.setColor(Color.blue);
		g.fillRect(x+1, (int) y+1, PADDLE_WIDTH-thickness, PADDLE_HEIGHT-thickness);
	}

	public void move() {	//move method
		if (upAccel) {
			yVel -= 2;
		}
		else if (downAccel) {
			yVel += 2;
		}
		else if (!upAccel && !downAccel) { //if keypad is released, slowly stop the paddle
			yVel *= FRICTION;
		}
		//sets the maximum velocity of the paddle
		if (yVel >= 6)
			yVel = 6;
		else if (yVel <= -6)
			yVel = -6;
		//moves the paddle by adding the velocity to it's y position
		y += yVel;
		
		if (y < 33)		//keeps the paddle from going over the top or bottom of the window
			y = 33;
		if (y > GAME_HEIGHT - PADDLE_HEIGHT - 10)
			y = GAME_HEIGHT - PADDLE_HEIGHT - 10;
		
		boundingBox.setBounds(x,(int)y, PADDLE_WIDTH+8, PADDLE_HEIGHT); //creates a perimeter around the paddle as it moves
	}
	
	public void setUpAccel (boolean input) {	//accerleration setters
		upAccel = input;
	}
	
	public void setDownAccel (boolean input) {
		downAccel = input;
	}
	
	public void setY(double i) {		//Y position setter
		this.y = i;
	}
	
	public int getY() {					//Y position getter
		return (int) y;
	}
	
	public Rectangle getBoundingBox() {	//perimeter of paddle getter
		return boundingBox;
	}
}

class AIPaddle implements Paddle {
	double y, yVel, ballTracker;
	boolean upAccel, downAccel;
	int player, x;
	MultiBall b;
	Rectangle boundingBox;
	
	public AIPaddle (int player, MultiBall b) {	//construct AI paddle
		this.b = b;
		upAccel = false;
		downAccel = false;
		y = GAME_HEIGHT/2 - PADDLE_HEIGHT/2;
		yVel = 0;
		x = GAME_WIDTH - PADDLE_WIDTH - PADDLE_BUFFER - BLOCK_WIDTH;
		
		boundingBox = new Rectangle(x,(int)y, PADDLE_WIDTH, PADDLE_HEIGHT);	//create a perimeter around the paddle
		boundingBox.setBounds(boundingBox);
	}

	public void draw(Graphics g) {	//draw method for the features
		Graphics2D g2 = (Graphics2D) g;
		int thickness = 2;
		g2.setStroke(new BasicStroke(thickness));
		g.setColor(Color.white);
		g.drawRect(x, (int) y, PADDLE_WIDTH, PADDLE_HEIGHT);
		
		g.setColor(Color.red);
		g.fillRect(x+1, (int) y+1, PADDLE_WIDTH-thickness, PADDLE_HEIGHT-thickness);
	}

	public void move() {}	//move method
	
	public void move(MultiBall b) {	//override move method

		ballTracker = GAME_HEIGHT/2;	//create a variable to keep track of the ball's position
		if(b.position.getX() >= (GAME_WIDTH/2) && b.Velocity.getX() > 0) //if ball is on right side of screen and moving toward AI paddle
			ballTracker = b.position.getY() + b.radius*2 - PADDLE_HEIGHT/2; //then set variable's value to be within a certain range
		//if the ball's range is within the paddle's range, then AI paddle with attempt to hit it
		if(ballTracker > y + PADDLE_HEIGHT/2 && b.position.getX() >= (GAME_WIDTH/2) && b.Velocity.getX() > 0) {
			if(ballTracker - yVel < y)
	            y = ballTracker;
	        else			//otherwise, the paddle will move toward vicinity of the ball
	            yVel += 2;
		}
		//same if statement as above, except the one above checks if the ball is above the paddle, this one checks if it's below
		else if(ballTracker < y - PADDLE_HEIGHT/2 && b.position.getX() >= (GAME_WIDTH/2) && b.Velocity.getX() > 0) {
			if(ballTracker + yVel > y)
	            y = ballTracker;
	        else
	            yVel -= 2;
		}
		//if the ball is moving away from the paddle, stop the paddle's movement
		if(b.Velocity.getX() < 0 && b.position.getX() >= (GAME_WIDTH/1.01)) {
			if(y - PADDLE_HEIGHT/2 > (GAME_WIDTH/2))
				yVel = 0;
			else if(y + PADDLE_HEIGHT/2 < (GAME_WIDTH/2))
				yVel = 0;
		}
		//sets the maximum velocity of the paddle
		if (yVel >= 5)
			yVel = 5;
		else if (yVel <= -5)
			yVel = -5;
		//moves the paddle by adding the velocity to it's y position
		y += yVel;
		//keeps the paddle from going above or below the window screen
		if (y < 33)
			y = 33;
		if (y > GAME_HEIGHT - PADDLE_HEIGHT - 10)
			y = GAME_HEIGHT - PADDLE_HEIGHT - 10;
		
		boundingBox.setBounds(x,(int)y, PADDLE_WIDTH, PADDLE_HEIGHT); //creates a perimeter as the paddle moves
	}
	
	public void setY(double i) {	//setter and getter of Y position
		this.y = i;
	}
	
	public int getY() {
		return (int) y;
	}
	
	public Rectangle getBoundingBox() {
		return boundingBox;
	}
}
//class for the balls
class MultiBall {
	static int GAME_WIDTH = 1000;
	static int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));
	static int PADDLE_WIDTH = 25;
	static int PADDLE_HEIGHT = 100;
	static int PADDLE_BUFFER = 25;
	public Color c;
	double speed;
	public int ballNum, radius, pcy, intersectY;
	public Vector2D position, Velocity;
	Rectangle boundingCir, boundingBlock;
	Blocks blockMap;
	Score score;
	double angle;
	static double maxAngle = 60;
	
	public MultiBall() {	//default constructor
		position = new Vector2D();
		Velocity = new Vector2D();
		position.set(GAME_WIDTH/2, GAME_HEIGHT/2);
		Velocity.set(getRandomSpeed() * getRandomDirection(), getRandomSpeed() * getRandomDirection());
		speed = getRandomSpeed();
		radius = 10;
		
		boundingCir = new Rectangle((int)position.getX(),(int)position.getY(), radius, radius);
		boundingCir.setBounds(boundingCir);
	}

	public MultiBall(int ballNumber, Color c) {	//override constructor
		this.ballNum = ballNumber;
		this.c = c;
		position = new Vector2D();
		Velocity = new Vector2D();
		position.set(GAME_WIDTH/2, GAME_HEIGHT/2);	//set the ball's position to center of the game
		Velocity.set(getRandomSpeed() * getRandomDirection(), getRandomSpeed() * getRandomDirection()); //set velocity and direction
		speed = getRandomSpeed();																		//to random values
		radius = 10;
		
		boundingCir = new Rectangle((int)position.getX(),(int)position.getY(), radius, radius); //create perimeter around the ball
		boundingCir.setBounds(boundingCir);
	}
	
	public double getRandomSpeed() {	//method to generate a random speed for the new ball
		return (Math.random() *3 +3);
	}
	
	public int getRandomDirection() {	//method to generate a random direction for the new ball
		int rand = (int)(Math.random() * 2);
		if (rand ==1)
			return 1;
		else
			return -1;
	}
	
	public void draw(Graphics g) { //draw method for the ball's features
		g.setColor(c);
		g.fillOval((int)position.getX()-(radius), (int)position.getY()-(radius), (radius*2), (radius*2));
		g.setColor(Color.white);
		g.drawOval((int)position.getX()-(radius), (int)position.getY()-(radius), (radius*2), (radius*2));		
	}
	
	public void checkPaddleCollision(Paddle p1, Paddle p2) { //method to check if the ball collides with either paddle
		
		if (Velocity.getX() < 0 && boundingCir.intersects(p1.getBoundingBox())) { //check for ball colliding with human paddle
			double angle = newVelocityAngle(p1);	//call a method to create a new angle for the ball to bounce
			double newVx = Math.abs((Math.cos(angle)) * this.speed); //new x variable to calculate the x direction and speed
			double newVy = (-Math.sin(angle)) * this.speed; //new y variable to calculate the y direction and speed
			double oldSign = Math.signum(Velocity.getX()); //get the old direction of x
			this.Velocity.setX(newVx * (-1 * oldSign)); //assign the new speed and opposite direction for x
			this.Velocity.setY(newVy);		//assign the new speed and direction for y
		}
		else if (Velocity.getX() > 0 && boundingCir.intersects(p2.getBoundingBox())) { //check for ball colliding with AI paddle
			double angle = newVelocityAngle(p2);
			double newVx = Math.abs((Math.cos(angle)) * this.speed);
			double newVy = (-Math.sin(angle)) * this.speed;
			double oldSign = Math.signum(Velocity.getX());
			this.Velocity.setX(newVx * (-1 *oldSign));
			this.Velocity.setY(newVy);
		}
	}
	
	public double newVelocityAngle(Paddle p) { //method to calculate the new angle for the ball to bounce
		@SuppressWarnings("static-access")
		double relIntersectY = ((p.getY() + (p.PADDLE_HEIGHT/2.0)) - (this.getY() - this.radius));
		@SuppressWarnings("static-access")
		double intersectY = relIntersectY / (p.PADDLE_HEIGHT/2.0);
		double angle = intersectY * maxAngle;
		return Math.toRadians(angle);
	}
	
	public void move(ArrayList<MultiBall> balls) { //move method
		for(MultiBall b : balls) {				//iterates through the arraylist of balls and check if they collide with each other
			if (b != this && this.colliding(b)) //if they do, then bounce accordingly
				this.resolveCollision(b);
		}
		
		if (position.getY() <= 45)				//keeps the balls within the window screen
			Velocity.setY(Velocity.getY() * -1);
		if (position.getY() >= GAME_HEIGHT-15)
			Velocity.setY(Velocity.getY() * -1);
		if (position.getX() <= 20)
			Velocity.setX(Velocity.getX() * -1);
		if (position.getX() >= GAME_WIDTH)
			Velocity.setX(Velocity.getX() * -1);
		
		position = position.add(Velocity);		//velocity is added to the ball's position and moves it
		
		boundingCir.setBounds((int)position.getX(),(int)position.getY(), radius, radius); //perimeter for the ball is created as it moves
	}
	
	public boolean colliding(MultiBall ball) {	//method to check if the balls collide
	    double xd = position.getX() - ball.position.getX();
	    double yd = position.getY() - ball.position.getY();

	    double sumRadius = radius + ball.radius;
	    double sqrRadius = sumRadius * sumRadius;

	    double distSqr = (xd * xd) + (yd * yd);

	    if (distSqr <= sqrRadius) {
	    	return true;
	    }

	    return false;
	}
	
	public void resolveCollision(MultiBall ball) {	//method to set new directions for the bounce if they collide
	    Vector2D delta = position.subtract(ball.position);	   
	    double d = delta.getLength();
	    // minimum translation distance to push balls apart after intersecting
	    Vector2D mtd = delta.multiply(((radius + ball.radius)-d)/d); 

	    // resolve intersection --
	    // inverse mass quantities
	    double im1 = 1 / 1; //If the balls have different masses you can use this
	    double im2 = 1 / 1; //my balls however all have the same mass

	    // push-pull them apart based off their mass
	    position = position.add(mtd.multiply(im1 / (im1 + im2)));
	    ball.position = ball.position.subtract(mtd.multiply(im2 / (im1 + im2)));

	    // impact speed
	    Vector2D v = (this.Velocity.subtract(ball.Velocity));
	    double vn = v.dot(mtd.normalize());

	    // sphere intersecting but moving away from each other already
	    if (vn > 0.0f) return;

	    // collision impulse
	    double i = (-(1.0f + 0.99f) * vn) / (im1 + im2);
	    Vector2D impulse = mtd.multiply(i);

	    // change in momentum
	    this.Velocity = this.Velocity.add(impulse.multiply(im1));
	    ball.Velocity = ball.Velocity.subtract(impulse.multiply(im2));
	}
	
	public int getX() {
		return (int) position.getX();
	}
	public int getY() {
		return (int) position.getY();
	}
}
//class for the bricks
class Blocks {
	static int GAME_WIDTH = 1000;
	static int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));
	static int BLOCK_HEIGHT = 79;
	static int BLOCK_WIDTH = 25;
	static int BLOCK_BUFFER = 7;
	public Vector2D position;
	int Side, yPos, count;
	int leftPos = 10, rightPos = GAME_WIDTH;
	int thickness = 2;
	int yIndex = 0;
	int buffer = 33;
	boolean isDestroyed = false;

	Score score = new Score(GAME_WIDTH, GAME_HEIGHT);
	MultiBall b;
	Rectangle boundingBlock;
	
	public Blocks(int index, int side) {	//construct each brick
		
		position = new Vector2D();
		
		if (index > 5) {
			yIndex = index - 6;
			count = index - 6;
		}
		else {
			yIndex = index;
			count = index;
		}
		
		buffer = buffer + (BLOCK_BUFFER*count);
		
		if (side == 0) {
			yPos = (BLOCK_HEIGHT * yIndex) + buffer;
			position.set(leftPos, yPos);
		}
		else if (side == 1) {
			yPos = (BLOCK_HEIGHT * yIndex) + buffer;
			position.set(rightPos-50, yPos);
		}
	}
	
	public void drawBlock(Graphics g) { //draw method for the brick's features and also draws a perimeter
		Graphics2D g2 = (Graphics2D) g;	
		
		g.setColor(Color.white);
		g2.setStroke(new BasicStroke(thickness));
		g.drawRect((int)position.getX(), (int)position.getY(), BLOCK_WIDTH, BLOCK_HEIGHT);
		g.setColor(Color.green);
		g.fillRect((int)position.getX()+1, (int)position.getY()+1, BLOCK_WIDTH-thickness, BLOCK_HEIGHT-thickness);
		boundingBlock = new Rectangle((int)position.getX(), (int)position.getY(), BLOCK_WIDTH, BLOCK_HEIGHT);
		boundingBlock.setBounds(boundingBlock);
	}
	
	public boolean checkBlockCollision(MultiBall ball, Blocks block) { //check if the ball collides with a brick
		//if the ball is on the right side of the screen and moving toward's the right and a brick exists, then...
		if (ball.position.getX() < GAME_WIDTH/2 && ball.Velocity.getX() < 0 && block != null) {
			if(ball.boundingCir.intersects(block.getBoundingBlock()) && block != null) {//check if the ball's and brick's perimeter collide
				ball.Velocity.setX(ball.Velocity.getX() * (-1));	//change the ball's direction
				block = null;	//set the brick as null
				boundingBlock.setBounds(0,0,0,0);	//remove the brick's perimeter
				return true;
			}	
		}
		//same as above, except on the left side of the screen
		else if (ball.position.getX() > GAME_WIDTH/2 && ball.Velocity.getX() > 0 && block != null) {
			if(ball.boundingCir.intersects(block.getBoundingBlock()) && block != null) {
				ball.Velocity.setX(ball.Velocity.getX() * (-1));
				block = null;
				boundingBlock.setBounds(0,0,0,0);
				return true;
			}
		}
		return false;
	}
	
	public boolean isDestroyed() {
		return isDestroyed;
	}
	
	public void setIsDestroyed(boolean value) {	//set the brick to true or false as "destroyed"
		isDestroyed = value;
	}
	
	public int getX() {
		return (int) position.getX();
	}
	
	public int getY() {
		return (int) position.getY();
	}
	
	public Rectangle getBoundingBlock() {
		return this.boundingBlock;
	}
}

// Author of Vector2D is Zaheer Ahmed
class Vector2D  {
    private double x;
    private double y;

    public Vector2D() {
        this.setX(0);
        this.setY(0);
    }

    public Vector2D(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    public void set(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {    	
        return y;
    }

    public double dot(Vector2D v2) {
    	double result = 0.0f;
        result = this.getX() * v2.getX() + this.getY() * v2.getY();
        return result;
    }

    public double getLength() {
        return (double) Math.sqrt(getX() * getX() + getY() * getY());
    }

    public Vector2D add(Vector2D v2) {
        Vector2D result = new Vector2D();
        result.setX(getX() + v2.getX());
        result.setY(getY() + v2.getY());
        return result;
    }

    public Vector2D subtract(Vector2D v2) {
        Vector2D result = new Vector2D();
        result.setX(this.getX() - v2.getX());
        result.setY(this.getY() - v2.getY());
        return result;
    }

    public Vector2D multiply(double scaleFactor) {
        Vector2D result = new Vector2D();
        result.setX(this.getX() * scaleFactor);
        result.setY(this.getY() * scaleFactor);
        return result;
    }

    public Vector2D normalize() {
    	double length = getLength();
        if (length != 0.0f) {
            this.setX(this.getX() / length);
            this.setY(this.getY() / length);
        } 
        else {
            this.setX(0.0f);
            this.setY(0.0f);
        }
        return this;
    }
    
    public String toString() {
    	return "("+x+", "+y+")";
    }
}
//class for the Score
class Score {
	static int WIDTH;
	static int HEIGHT;
	public int player1;
	public int player2;
	
	Score (int width, int height){	//construct the Score and get the game's window size
		Score.WIDTH = width;
		Score.HEIGHT = height;
	}
	
	public void player1Score() {	//getters and setters for the player's points
		this.player1++;
	}
	
	public void player2Score() {
		this.player2++;
	}
	
	public void setPlayer1Score(int i) {
		player1 = i;
	}
	
	public void setPlayer2Score(int i) {
		player2 = i;
	}
	
	public int getPlayer1Score() {
		return player1;
	}
	
	public int getPlayer2Score() {
		return player2;
	}
	
	public void draw (Graphics g) {	//draw and keep track of the Score in the top middle of the window
		g.setColor(Color.white);
		g.setFont(new Font("Consolas", Font.PLAIN, 40));
		g.drawLine(WIDTH/2, 0, WIDTH/2, HEIGHT);
		g.drawString(String.valueOf(player1), (WIDTH/2)-50, 70);
		g.drawString(String.valueOf(player2), (WIDTH/2)+25, 70);
	}
}