package Client.Game;
import Common.Vector2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import static Common.Constants.*;


public class Paddle extends Thread{
	public boolean isGameRunning;

	public static final int width = 15;
	public static final int height = 100;

	Vector2D paddlePosition;

	public boolean isPlayerPaddle;
	public int yDirection;
	int speed = 5;

	Rectangle paddle;

	public Paddle(int x, int y, boolean isPlayerPaddle){
		isGameRunning = true;

		this.isPlayerPaddle = isPlayerPaddle;
		paddlePosition = new Vector2D(x,y);
		paddle = new Rectangle(x, y, width, height);
	}

	public void keyPressed(KeyEvent e) {
		if (isPlayerPaddle){
			if(e.getKeyCode() == KeyEvent.VK_UP) {
				setYDirection(-1);
			}
			if(e.getKeyCode() == KeyEvent.VK_DOWN) {
				setYDirection(1);
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (isPlayerPaddle){
			if(e.getKeyCode() == KeyEvent.VK_UP) {
				setYDirection(0);
			}
			if(e.getKeyCode() == KeyEvent.VK_DOWN) {
				setYDirection(0);
			}
		}
	}
	public void setYDirection(int yDir) {
		yDirection = yDir;
	}

	public void move() {
//		if (!isPlayerPaddle){
//			System.out.println(yDirection);
//		}
		paddle.y += yDirection * speed;
		if (paddle.y <= 25)
			paddle.y = 25;
		if (paddle.y >= SCREEN_Y-25)
		paddle.y =  SCREEN_Y-25;
	}
	public void draw(Graphics g) {
		g.setColor(Color.pink);
		g.fillRect(paddle.x, paddle.y, paddle.width, paddle.height);
	}

	@Override
	public void run() {
		try {
			while(isGameRunning) {
				move();
				Thread.sleep(TICKS);
			}
			System.out.println("PADDLE STOPPED");
		} catch(Exception e) { System.err.println(e.getMessage()); }
	}




}