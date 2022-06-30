package Client.Game;


import Client.Socket.UdpClientSocket;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

import static Common.Constants.*;
import static Common.Protocol.Protocol.*;
import Common.JsonSerialize;


public class Pong extends JFrame {
    public UdpClientSocket clientSocket;
    public boolean isGameRunning;

    private Dimension screenSize = new Dimension(SCREEN_X, SCREEN_Y);

    private Image dbImage;
    private Graphics dbGraphics;

    private boolean isPressedPaddleMoveKey = false;

    private static Ball b;

    public Pong(UdpClientSocket socket) {
        isGameRunning = true;

        this.setTitle("Pong!");
        this.setSize(screenSize);
        this.setResizable(false);
        this.setBackground(Color.DARK_GRAY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addKeyListener(new AL());

        this.clientSocket = socket;


        Paddle paddle1 = null;
        Paddle paddle2 = null;

        System.out.println(clientSocket.clientGameController.playerNumberInGame);

        switch (clientSocket.clientGameController.playerNumberInGame){
            case(1):
                paddle1 = new Paddle(BORDER_GAP +10,SCREEN_Y/2-18,true);
                paddle2 = new Paddle(SCREEN_X - BORDER_GAP -10,SCREEN_Y/2,false);
                break;

            case(2):
                paddle1 = new Paddle(BORDER_GAP +10,SCREEN_Y/2-18,false);
                paddle2 = new Paddle(SCREEN_X - BORDER_GAP -10,SCREEN_Y/2,true);
                break;
        }
        b = new Ball(paddle1,paddle2,clientSocket);
        b.start();

        this.setVisible(true);

        new connectionThread().start();

        clientSocket.clientGameController.sendReadyForGame(clientSocket.roomController.thisClient);
    }

    public class connectionThread extends Thread {
        @Override
        public synchronized void run() {
            setName("connectionThread");
            waitGameSocketMsg();
        }

        public void waitGameSocketMsg() {
            synchronized (clientSocket.gameMonitor) {
                while (clientSocket.isConnected() && isGameRunning) {
                    try {
                        clientSocket.gameMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    switch (clientSocket.clientGameController.commandForGameUI) {
                        case (PADDLE_REPAINT):
                            if (clientSocket.clientGameController.receivedPacket.gamePaddle.pNum == 1){
                                b.p1.setYDirection(clientSocket.clientGameController.receivedPacket.gamePaddle.yDir);
                            }
                            else {
                                b.p2.setYDirection(clientSocket.clientGameController.receivedPacket.gamePaddle.yDir);
                            }
                            break;

                        case(GET_BALL_ANGLE):
                            b.angle = clientSocket.clientGameController.receivedPacket.gameBall.angle;
                            break;

                        case(GET_BALL_INIT):
                            System.out.println(new JsonSerialize().toJson(clientSocket.clientGameController.receivedPacket));

                            b.initBall();
                            b.setDestDelta(clientSocket.clientGameController.receivedPacket.gameBall.x,clientSocket.clientGameController.receivedPacket.gameBall.y);
                            b.score = clientSocket.clientGameController.receivedPacket.msg;
                            break;

                        case(GET_BALL_DESTINATION):
                            b.setDestDelta(clientSocket.clientGameController.receivedPacket.gameBall.x,clientSocket.clientGameController.receivedPacket.gameBall.y);
                            break;

                        case(END_GAME):
                            System.out.println("NO GAME");
                            b.isGameRunning = false;

                            break;
                    }
                }

            }
        }
    }

    @Override
    public void paint(Graphics g) {
        dbImage = createImage(getWidth(), getHeight());
        dbGraphics = dbImage.getGraphics();
        draw(dbGraphics);
        g.drawImage(dbImage, 0, 0, this);
    }

    public void draw(Graphics g) {
        b.draw(g);
        b.p1.draw(g);
        b.p2.draw(g);

        g.setColor(Color.WHITE);

        Font currentFont = g.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
        g.setFont(newFont);

        g.drawString("score: "+b.score,SCREEN_X/2,100);
//        g.drawString(""+b.p2score, 385, 20);
//        g.drawString(""+b.p1score, 15, 20);

        repaint();
    }

    public class AL extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            b.p1.keyPressed(e);
            b.p2.keyPressed(e);
            if (b.p1.isPlayerPaddle && !isPressedPaddleMoveKey){
                clientSocket.clientGameController.sendPaddleDirection(b.p1.yDirection);
            }
            if (b.p2.isPlayerPaddle && !isPressedPaddleMoveKey){
                clientSocket.clientGameController.sendPaddleDirection(b.p2.yDirection);
            }
            isPressedPaddleMoveKey = true;
        }
        @Override
        public void keyReleased(KeyEvent e) {
            b.p1.keyReleased(e);
            b.p2.keyReleased(e);
            if (b.p1.isPlayerPaddle && isPressedPaddleMoveKey){
                clientSocket.clientGameController.sendPaddleDirection(b.p1.yDirection);
            }
            if (b.p2.isPlayerPaddle && isPressedPaddleMoveKey){
                clientSocket.clientGameController.sendPaddleDirection(b.p2.yDirection);
            }
            isPressedPaddleMoveKey = false;
        }

    }


}
