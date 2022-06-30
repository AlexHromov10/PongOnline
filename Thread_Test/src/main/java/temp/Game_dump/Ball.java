package temp.Game_dump;

import Common.UdpLibriary.UdpSocketExtension;
import Common.Vector2D;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static Common.Constants.*;


public class Ball implements Runnable {
	public boolean isGameRunning;

	private int PADDLE_GAP = BORDER_GAP +10+ Paddle.width;

	Paddle p1 = new Paddle(BORDER_GAP +10,SCREEN_Y/2-18,1);
	Paddle p2 = new Paddle(SCREEN_X - BORDER_GAP -10,SCREEN_Y/2,2);

	Rectangle ballSprite;

	Vector2D ballDestination;
	Vector2D ballPrevPosition;

	double deltaX;
	double deltaY;
	double angle;

	double buffX;
	double buffY;

	double speed = 3;

	public Ball()
	{
		isGameRunning = true;
		ballSprite = new Rectangle(SCREEN_X/2, SCREEN_Y/2, 15, 15);
		initBall();
	}

	public void initBall(){
		ballSprite.x = SCREEN_X/2;
		ballSprite.y = SCREEN_Y/2;

		ballPrevPosition = new Vector2D();

		setRandomDirection();
		setDeltaAnglePrev(ballSprite.x,ballSprite.y);
		speed = 3;
	}

	public void setDeltaAnglePrev(int nowPosX,int nowPosY){
		deltaX = ballDestination.x - nowPosX;
		deltaY = ballDestination.y - nowPosY;
		angle = Math.atan2( deltaY, deltaX );

		ballPrevPosition.set(nowPosX, nowPosY);

	}

	public void setRandomDirection(){
		boolean isLeft = ThreadLocalRandom.current().nextBoolean();
		int randomY = ThreadLocalRandom.current().nextInt(0, SCREEN_Y);
		if (isLeft){
			ballDestination = new Vector2D(0,randomY);
		}
		else{
			ballDestination = new Vector2D(SCREEN_X,randomY);
		}
		//p1.setYDirection(1);
	}

	public void draw(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(ballSprite.x,ballSprite.y, ballSprite.width, ballSprite.height);
	}

	public void collision(){
		if(ballSprite.intersects(p1.paddle))
			reflect(p1);
		if(ballSprite.intersects(p2.paddle))
			reflect(p2);
	}

	public void reflect(Paddle p){
		double hitPoint = ballSprite.getCenterY() - p.paddle.getCenterY();

		double newX = (hitPoint*2/(p.paddle.getHeight()))*(-1);
		double newY = ((hitPoint*2/(p.paddle.getHeight()))+1)/2;

		if (newY < 0){
			newY = 0;
		}
		if (newY > 1){
			newY = 1;
		}

		if (newX < PADDLE_GAP ){
			ballDestination.set(PADDLE_GAP ,newY*SCREEN_Y);
		}
		else if (newX> SCREEN_X- PADDLE_GAP){
			ballDestination.set(SCREEN_X-PADDLE_GAP ,newY*SCREEN_Y);
		}
		else {
			ballDestination.set(newX ,newY*SCREEN_Y);
		}
		setDeltaAnglePrev(ballSprite.x,ballSprite.y);
		speed*=1.1;

	}

	public void move() {
		collision();

		// Коснулся слева. ПРАВЫЙ ВЫИГРАЛ
		if (ballSprite.x < BORDER_GAP +p1.paddle.width-3) {
			System.out.println("LEFT LOOSE");
			initBall();
		}

		// Коснулся справа. ЛЕВЫЙ ВЫИГРАЛ
		if (ballSprite.x > SCREEN_X- BORDER_GAP -p2.paddle.width+3) {
			System.out.println("RIGHT LOOSE");
			initBall();
		}

		// Коснулся сверху
		if (ballSprite.y < BORDER_GAP) {
			ballDestination.set(ballSprite.x +(ballSprite.x - ballPrevPosition.x), ballPrevPosition.y );
			ballSprite.y = BORDER_GAP +5;
			setDeltaAnglePrev(ballSprite.x, ballSprite.y );
		}

		// Коснулся снизу
		if (ballSprite.y > SCREEN_Y- BORDER_GAP) {
			ballDestination.set(ballSprite.x +(ballSprite.x - ballPrevPosition.x), ballPrevPosition.y );
			ballSprite.y = SCREEN_Y- BORDER_GAP -5;
			setDeltaAnglePrev(ballSprite.x, ballSprite.y);
		}

		buffX = Math.cos(angle);
		buffY = Math.sin(angle);
		if (buffX<1 && buffX>0){
			buffX = 1;
		}
		if (buffX>-1 && buffX<0){
			buffX = -1;
		}

		ballSprite.x =  (int)(ballSprite.x + speed * buffX);
		ballSprite.y = (int)(ballSprite.y + speed * buffY);
	}


	@Override
	public void run() {
		try {

			while(isGameRunning) {
				move();
				Thread.sleep(TICKS);
			}
		}catch(Exception e) { System.err.println(e.getMessage()); }
	}
}