package Client.Game;

import Client.Socket.UdpClientSocket;
import Common.Protocol.GameBall;
import Common.Protocol.Packet;
import Common.UdpLibriary.UdpSocketExtension;
import Common.Vector2D;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static Common.Constants.*;
import static Common.Protocol.Protocol.GET_BALL_ANGLE;
import static Common.Protocol.Protocol.SEND_BALL_REFLECTION;


public class Ball extends Thread{
	private UdpClientSocket clientSocket;
	public boolean isGameRunning;

	private int PADDLE_GAP = BORDER_GAP +10+Paddle.width;

	String score;

//	Paddle p1 = new Paddle(BORDER_GAP +10,SCREEN_Y/2-18,1);
//	Paddle p2 = new Paddle(SCREEN_X - BORDER_GAP -10,SCREEN_Y/2,2);
	Paddle p1;
	Paddle p2;

	Rectangle ballSprite;

	Vector2D ballDestination;
	Vector2D ballPrevPosition;

	double deltaX;
	double deltaY;
	Double angle;

	double buffX;
	double buffY;

	double speed = 3;

	public Ball(Paddle p1, Paddle p2, UdpClientSocket clientSocket)
	{
		isGameRunning = true;

		this.clientSocket = clientSocket;
		this.p1 = p1;
		this.p2 = p2;

		angle = null;
		speed = 3;

		this.p1.start();
		this.p2.start();
		this.p1.setName("PADDLE_1");
		this.p1.setName("PADDLE_2");
		setName("BALL");

		initBall();
	}

	public void initBall(){
		ballSprite = new Rectangle(SCREEN_X/2, SCREEN_Y/2, 15, 15);

		ballSprite.x = SCREEN_X/2;
		ballSprite.y = SCREEN_Y/2;

		ballPrevPosition = new Vector2D();
		ballDestination = new Vector2D();
	}

	public void draw(Graphics g) {
		if (ballSprite != null){
			g.setColor(Color.WHITE);
			g.fillRect(ballSprite.x,ballSprite.y, ballSprite.width, ballSprite.height);
		}
	}

	public void setDeltaAnglePrev(int nowPosX,int nowPosY){
		deltaX = ballDestination.x - nowPosX;
		deltaY = ballDestination.y - nowPosY;
		angle = Math.atan2( deltaY, deltaX );

		ballPrevPosition.set(nowPosX, nowPosY);
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
			//ballDestination.set(PADDLE_GAP ,newY*SCREEN_Y);
//			clientSocket.clientGameController.sendDestinationAfterReflect(PADDLE_GAP ,newY*SCREEN_Y);
			setBallDestination(PADDLE_GAP ,newY*SCREEN_Y);
		}
		else if (newX> SCREEN_X- PADDLE_GAP){
			//ballDestination.set(SCREEN_X-PADDLE_GAP ,newY*SCREEN_Y);
//			clientSocket.clientGameController.sendDestinationAfterReflect(SCREEN_X-PADDLE_GAP ,newY*SCREEN_Y);
			setBallDestination(SCREEN_X-PADDLE_GAP ,newY*SCREEN_Y);
		}
		else {
			//ballDestination.set(newX ,newY*SCREEN_Y);
//			clientSocket.clientGameController.sendDestinationAfterReflect(newX ,newY*SCREEN_Y);
			setBallDestination(newX ,newY*SCREEN_Y);
		}
		//speed*=1.1;

	}


	public void move() {
		collision();

		if (ballSprite.x < BORDER_GAP +p1.width-3) {
			//System.out.println("LEFT LOOSE");
			clientSocket.clientGameController.sendEndRound(true);
			initBall();
		}

		// Коснулся справа. ЛЕВЫЙ ВЫИГРАЛ
		if (ballSprite.x > SCREEN_X- BORDER_GAP -p2.width+3) {
			clientSocket.clientGameController.sendEndRound(false);
			initBall();
		}

		// Коснулся сверху
		if (ballSprite.y < BORDER_GAP) {
			System.out.println("BALL UP");
			ballSprite.y = BORDER_GAP +5;
			setBallDestination(ballSprite.x +(ballSprite.x - ballPrevPosition.x), ballPrevPosition.y );
		}

		// Коснулся снизу
		if (ballSprite.y > SCREEN_Y- BORDER_GAP) {
			System.out.println("BALL DOWN");
			ballSprite.y = SCREEN_Y- BORDER_GAP -5;
			setBallDestination(ballSprite.x +(ballSprite.x - ballPrevPosition.x), ballPrevPosition.y );
		}

		if (angle != null){
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
	}


	public void setBallDestination(double x,double y){
		clientSocket.clientGameController.sendDestination(x,y);
		setDestDelta(x,y);
	}

	public void setDestDelta(double x,double y){
		ballDestination.set(x,y);
		setDeltaAnglePrev(ballSprite.x, ballSprite.y);
	}

	public void shutdown(){
		//blabla
	}

	@Override
	public void run() {
		try {
			while(isGameRunning) {
				move();
				Thread.sleep(TICKS);
			}
			System.out.println("BALL STOPPED");
			p1.isGameRunning = false;
			p2.isGameRunning = false;
		}catch(Exception e) { System.err.println(e.getMessage()); }
	}
}