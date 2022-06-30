package temp;

import Common.Protocol.GameBall;
import Common.Protocol.Packet;
import Common.Vector2D;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static Common.Constants.*;
import static Common.Constants.BORDER_GAP;
import static Common.Protocol.Protocol.GET_BALL_ANGLE;
import static Common.Protocol.Protocol.GET_BALL_INIT;

public class DUMP_lol {


    //    SwingWorker<Void, Void>  connectionWorker = new  SwingWorker<Void, Void>() {
//        @Override
//        protected synchronized Void doInBackground() {
//            while(!clientSocket.isConnected() && !isBadConnectionOrHost) {
//                try {
//                    System.out.println("connectionWorker wait");
//                    wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            System.out.println("RETURNED");
//            return null;
//        }
//
//        protected  void done() {
//            if (clientSocket.isConnected()){
//                cl.show(panel,"roomPanelCard");
//            }
//            if (clientSocket.isUnknownHost_or_BadConnection()){
//                cl.show(panel,"loadingPanelCard");
//                loadingPanelLabel.setText("ERROR! TimedOut");
//            }
//
//        }
//    };

















    //    public void connectedSocket(String received){
//        try {
//            Packet packetDecode = JSON.parseObject(received, Packet.class);
//
//            packetDecode.client.print();
//
//            if (!packetDecode.command.equals(SUCCESS_CONNECT)){
//                System.out.println("ERROR");
//                System.out.println(packetDecode.command);
//                return;
//            }
//
//            serverClientSocketPort = packetDecode.client.serverPort;
//            System.out.println("Connected. Server port: " + received);
//            connectButton.setEnabled(false);
//
//            isConnected = true;
//
//            synchronized (monitor) {
//                monitor.notify();
//            }
//
//            while (isRunning) {
//                String receivedText = udpSocket.receiveData();
//
//                if (received.equals("/QUIT")) {
//                    isRunning = false;
//                    break;
//                }
//
//                System.out.println("> " + receivedText);
//            }
//        } catch (NumberFormatException e) {
//            System.out.println("NOT A PORT!");
//        }
//
//    }








    //    public static void main(String[] args) {
//        Client client = null;
//        try {
//            client = new Client.Builder("ALEX")
//                    .withIP_Port_CLIENT(InetAddress.getByName("localhost"),25565)
//                    .build();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        Packet packet = new Packet.Builder("CONNECT")
//                .withClient(client)
//                .build();
//
//        String jsonString = JSON.toJSONString(packet);
//        System.out.println(jsonString);
//
//        Packet packetDecode = JSON.parseObject(jsonString, Packet.class);
//        System.out.println(packetDecode.client.nickname+" ; "+packetDecode.command);
//    }





    //    public void connectPlayer(Client client){
//        try {
//            UdpClientConnectionHandler connectedClient = new UdpClientConnectionHandler(freePort, clientsFreeIDs);
//            connectedClient.start();
//
//            Packet packet = new Packet.Builder(SUCCESS_CONNECT)
//                    .withClient(client)
//                    .build();
//
//            udpSocket.sendDataToClient(packet);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//
//    }





//
//
//
//    public class BallMoveLogic extends Thread{
//        @Override
//        public void run(){
//            try {
//                initBall();
//                while(isConnectionRunning) {
//                    move();
//                    Thread.sleep(TICKS);
//                }
//            }catch(Exception e) { System.err.println(e.getMessage()); }
//        }
//
//        public void initBall(){
//            ballSprite = new Rectangle(SCREEN_X/2, SCREEN_Y/2, 15, 15);
//
//            ballSprite.x = SCREEN_X/2;
//            ballSprite.y = SCREEN_Y/2;
//
//            ballPrevPosition = new Vector2D();
//            ballDestination = new Vector2D();
//
//            angle = null;
//            sendInitBall();
//            setRandomDirection();
//            speed = 3;
//        }
//
//        public void setRandomDirection(){
//            boolean isLeft = ThreadLocalRandom.current().nextBoolean();
//            int randomY = ThreadLocalRandom.current().nextInt(0, SCREEN_Y);
//            if (isLeft){
//                setBallDestination(0,randomY);
//            }
//            else{
//                setBallDestination(SCREEN_X,randomY);
//            }
//        }
//
//        public void setDeltaAnglePrev(int nowPosX,int nowPosY){
//            deltaX = ballDestination.x - nowPosX;
//            deltaY = ballDestination.y - nowPosY;
//            angle = Math.atan2( deltaY, deltaX );
//
//            ballPrevPosition.set(nowPosX, nowPosY);
//        }
//
//        public void move() {
//            //System.out.println(ballSprite.x + " ; "+ ballSprite.y );
//            // Коснулся слева. ПРАВЫЙ ВЫИГРАЛ
//            if (ballSprite.x < BORDER_GAP +p1.width-3) {
//                //System.out.println("LEFT LOOSE");
//                initBall();
//            }
//
//            // Коснулся справа. ЛЕВЫЙ ВЫИГРАЛ
//            if (ballSprite.x > SCREEN_X- BORDER_GAP -p2.width+3) {
//                System.out.println(SCREEN_X- BORDER_GAP -p2.width+3);
//                initBall();
//            }
//
//            // Коснулся сверху
//            if (ballSprite.y < BORDER_GAP) {
//                System.out.println("BALL UP");
//                ballSprite.y = BORDER_GAP +5;
//                setBallDestination(ballSprite.x +(ballSprite.x - ballPrevPosition.x), ballPrevPosition.y );
//            }
//
//            // Коснулся снизу
//            if (ballSprite.y > SCREEN_Y- BORDER_GAP) {
//                System.out.println("BALL DOWN");
//                ballSprite.y = SCREEN_Y- BORDER_GAP -5;
//                setBallDestination(ballSprite.x +(ballSprite.x - ballPrevPosition.x), ballPrevPosition.y );
//            }
//
//            if (angle != null){
//                buffX = Math.cos(angle);
//                buffY = Math.sin(angle);
//                if (buffX<1 && buffX>0){
//                    buffX = 1;
//                }
//                if (buffX>-1 && buffX<0){
//                    buffX = -1;
//                }
//
//                ballSprite.x =  (int)(ballSprite.x + speed * buffX);
//                ballSprite.y = (int)(ballSprite.y + speed * buffY);
//            }
//        }
//
//        public void setBallDestination(double x,double y){
//            ballDestination.set(x,y);
//            setDeltaAnglePrev(ballSprite.x, ballSprite.y);
//
//            Packet sendPacket = new Packet.Builder(GET_BALL_ANGLE)
//                    .withGameBall(new GameBall(angle))
//                    .build();
//            sendToClients(sendPacket);
//        }
//
//        public void sendInitBall(){
//            Packet sendPacket = new Packet.Builder(GET_BALL_INIT)
//                    .build();
//            sendToClients(sendPacket);
//        }
//    }\
















}
