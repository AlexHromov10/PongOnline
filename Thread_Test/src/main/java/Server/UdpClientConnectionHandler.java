package Server;

import Common.JsonSerialize;
import Common.Protocol.Client;
import Common.Protocol.GameBall;
import Common.Protocol.Packet;
import Common.UdpLibriary.UdpSocketExtension;

import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import static Common.Constants.SCREEN_X;
import static Common.Constants.SCREEN_Y;
import static Common.Protocol.Protocol.*;

public class UdpClientConnectionHandler extends Thread{
    private UdpSocketExtension udpSocket;
    private JsonSerialize json = new JsonSerialize();
    public int PORT;
    public Client client1;
    public Client client2;

    boolean isConnectionRunning;

    int readyCount = 0;

    int p1score = 0;
    int p2score = 0;

    boolean blockingBool = false;


    public UdpClientConnectionHandler(int PORT) throws SocketException {
        udpSocket = new UdpSocketExtension(PORT);
        isConnectionRunning = true;
        this.PORT = PORT;
    }

    @Override
    public void run(){
        System.out.println("GAME ON!!!!!!!!");

        while (isConnectionRunning){
            Packet receivedPacket = udpSocket.receivePacketData();
            System.out.println(">GAME<: "+json.toJson(receivedPacket));
            receivePacket(receivedPacket);
        }
        //return;
        //sendEndGame();
    }

    public void receivePacket(Packet packet){
        switch (packet.command){
            case(READY_FOR_GAME):
                readyCount++;
                tryStartGame();
                break;

            case(SEND_PADDLE_Y_DIR):
                receivedGamePaddle(packet);
                break;

            case(SEND_BALL_REFLECTION):
                receivedReflection(packet);
                break;

            case(SEND_BALL_DESTINATION):
                receivedDestination(packet);
                break;

            case(LEFT_WON):
                if (!blockingBool){
                    blockingBool = true;
                    scoreUp(true);
                    activateInitBallTimer();
                }
                break;

            case(RIGHT_WON):
                if (!blockingBool){
                    blockingBool = true;
                    scoreUp(false);
                    activateInitBallTimer();
                }
                break;
        }
    }

    public void tryStartGame(){
        if (readyCount >=2){
            sendInitBall();
            readyCount = 0;
        }
    }

    public void sendEndGame(){
        isConnectionRunning = false;
        Packet sendPacket = new Packet.Builder(END_GAME)
                .build();
        sendToClients(sendPacket);
    }

    public GameBall setRandomDirection(){
            boolean isLeft = ThreadLocalRandom.current().nextBoolean();
            int randomY = ThreadLocalRandom.current().nextInt(0, SCREEN_Y);
            if (isLeft){
                //setBallDestination(0,randomY);
                return new GameBall(0,randomY);
            }
            else{
                //setBallDestination(SCREEN_X,randomY);
                return new GameBall(SCREEN_X,randomY);
            }
        }

    public void sendInitBall(){

            System.out.println("!!! START GAME");
            Packet sendPacket = new Packet.Builder(GET_BALL_INIT)
                    .withMsg(p1score+";"+p2score)
                    .withGameBall(setRandomDirection())
                    .build();
            sendToClients(sendPacket);

            setRandomDirection();

    }
    public void activateInitBallTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                blockingBool = false;
                timer.cancel();
            }
        }, 1000);
    }

    public void scoreUp(boolean isLeftWon){
        if (isLeftWon){
            p1score++;
        }
        else{
            p2score++;
        }

        if (p1score>=5 || p2score>=5){
            sendEndGame();
        }
        else {
            sendInitBall();
        }
    }

    public Client getOpponent(int myPNum){
        if (myPNum == 1){
            return client2;
        }
        return client1;
    }

    public void receivedGamePaddle(Packet packet){
        System.out.println("RECEIVED PADDLE");
        if (packet.gamePaddle.yDir == -1 || packet.gamePaddle.yDir == 0 || packet.gamePaddle.yDir == 1){
            packet.command = GET_OPPONENT_PADDLE_Y_DIR;
            udpSocket.sendDataToClient(packet, getOpponent(packet.gamePaddle.pNum));
        }
    }

    public void receivedReflection(Packet packet){
        //ballMoveLogic.setBallDestination(packet.gameBall.x,packet.gameBall.y);
    }

    public void receivedDestination(Packet packet){
        packet.command = GET_BALL_DESTINATION;
        sendToClients(packet);
    }

    public void sendToClients(Packet packet){
        System.out.println("SENDED TO CLIENTS");
        udpSocket.sendDataToClient(packet, client1);
        udpSocket.sendDataToClient(packet, client2);
    }

}
