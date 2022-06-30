package temp.Game_dump;

import Common.Vector2D;

import java.awt.*;
import java.awt.event.KeyEvent;

import static Common.Constants.SCREEN_Y;
import static Common.Constants.TICKS;


public class Paddle implements Runnable{

	public static final int width = 15;
	public static final int height = 100;

	Vector2D paddlePosition;

	int yDirection, id;
	int speed = 5;

	Rectangle paddle;

	public Paddle(int x, int y, int id){
		paddlePosition = new Vector2D(x,y);
		this.id = id;
		paddle = new Rectangle(x, y, width, height);
	}

	public void keyPressed(KeyEvent e) {
		switch(id) {
			default:
				System.out.println("Please enter a Valid ID in paddle contructor");
				break;
			case 1:
				if(e.getKeyCode() == KeyEvent.VK_W) {
					setYDirection(-1);
				}
				if(e.getKeyCode() == KeyEvent.VK_S) {
					setYDirection(1);
				}
				break;

			case 2:
				if(e.getKeyCode() == KeyEvent.VK_UP) {
					setYDirection(-1);
				}
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					setYDirection(1);
				}
				break;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch(id) {
			default:
				System.out.println("Please enter a Valid ID in paddle contructor");
				break;
			case 1:
				if(e.getKeyCode() == e.VK_W) {
					setYDirection(0);
				}
				if(e.getKeyCode() == e.VK_S) {
					setYDirection(0);
				}
				break;

			case 2:
				if(e.getKeyCode() == e.VK_UP) {
					setYDirection(0);
				}
				if(e.getKeyCode() == e.VK_DOWN) {
					setYDirection(0);
				}
				break;
		}
	}
	public void setYDirection(int yDir) {
		yDirection = yDir;
	}

	public void move() {
		paddle.y += yDirection * speed;
		if (paddle.y <= 25)
			paddle.y = 25;
		if (paddle.y >= SCREEN_Y-25)
		paddle.y =  SCREEN_Y-25;
	}
	public void draw(Graphics g) {
		switch(id) {
			default:
				System.out.println("Please enter a Valid ID in paddle contructor");
				break;
			case 1:
				g.setColor(Color.CYAN);
				g.fillRect(paddle.x, paddle.y, paddle.width, paddle.height);
				break;
			case 2:
				g.setColor(Color.pink);
				g.fillRect(paddle.x, paddle.y, paddle.width, paddle.height);
				break;
		}
	}
	@Override
	public void run() {
		try {
			while(true) {
				move();
				Thread.sleep(TICKS);
			}
		} catch(Exception e) { System.err.println(e.getMessage()); }
	}




}