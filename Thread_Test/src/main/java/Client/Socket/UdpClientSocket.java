package Client.Socket;

import Common.JsonSerialize;
import Common.Protocol.*;
import Common.UdpLibriary.UdpSocketExtension;

import javax.swing.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

import static Common.Protocol.Protocol.*;


public class UdpClientSocket extends Thread{
    private UdpSocketExtension udpSocket;
    public ClientRoomController roomController;
    public ClientGameController clientGameController = null;

    public JButton connectButton;

    private String nickname;
    private InetAddress mainServerIP;
    private  int mainServerPort;

    private boolean isRunning;
    private boolean isConnected;
    private boolean isConnectionProblem;
    public String userInputProblem;

    private JsonSerialize json = new JsonSerialize();

    private boolean isGetALIVE_SERVER = false;
    private java.util.Timer connectionTryTimer = null;
    private java.util.Timer connectionCheckTimer = null;
    private static final int CONNECTION_CHECK_DELAY_MS = 5000;

    public final Object connectionMonitor = new Object();
    public final Object socketActionMonitor = new Object();
    public final Object gameMonitor = new Object();

    public UdpClientSocket(String nickname,String IP,int port, JButton connectButton) throws SocketException, UnknownHostException {
        this.udpSocket = new UdpSocketExtension();

        this.nickname = nickname;
        this.mainServerIP = InetAddress.getByName(IP);
        this.mainServerPort = port;

        this.connectButton = connectButton;

        this.isConnected = false;
        this.isConnectionProblem = false;
        this.userInputProblem = null;

        this.roomController = new ClientRoomController(udpSocket,mainServerIP);
    }

    public boolean isConnected(){
        return isConnected;
    }

    public boolean isConnectionProblem(){
        return isConnectionProblem;
    }

    @Override
    public void run(){
        isRunning = true;
        activateConnectionTryTimer();

        Client client = new Client.Builder(nickname)
                .build();
        Packet packet = new Packet.Builder(CLIENT_CONNECT_MSG)
                .withClient(client)
                .build();

        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, mainServerPort);
        Packet packetDecode = udpSocket.receivePacketData();

        receiveCommand(packetDecode);
    }

    public void receiveCommand(Packet packet){
        switch (packet.command){
            case(ALIVE_SERVER):
                isGetALIVE_SERVER = true;
                break;

            case (SERVER_OFFLINE):
                System.out.println("LOST CONNECTION TO SERVER!!!");
                isRunning = false;
                break;

            case(NICKNAME_IS_TAKEN):
                wrongUserInput(NICKNAME_IS_TAKEN);
                break;

            case(NICKNAME_IS_EMPTY):
                wrongUserInput(NICKNAME_IS_EMPTY);
                break;

            case (UNKNOWN_ERROR):
                System.out.println("ERROR: "+packet.msg);
                break;

            case (SUCCESS_CONNECT):
                roomController.thisClient = packet.client;
                if (!isConnected){
                    roomController.requestRooms();
                    connectedSocket();
                }
                break;

            case (RETURN_ROOMS):
                roomController.getRooms(packet,socketActionMonitor);
                break;

            case (NO_ROOMS):
                roomController.noRooms(socketActionMonitor);
                break;

            case(CONNECT_TO_A_NEW_SOCKET):
                clientGameController = new ClientGameController(udpSocket,mainServerIP,packet.gamePaddle.pNum,Integer.parseInt(packet.msg));
                clientGameController.firstConnection(packet.gamePaddle,packet.client);
                break;

            case(GET_OPPONENT_PADDLE_Y_DIR):
                clientGameController.repaintGameUI(packet,PADDLE_REPAINT,gameMonitor);
                break;

            case(GET_BALL_ANGLE):
                clientGameController.repaintGameUI(packet,GET_BALL_ANGLE,gameMonitor);
                break;

            case(GET_BALL_INIT):
                clientGameController.repaintGameUI(packet,GET_BALL_INIT,gameMonitor);
                break;

            case (END_GAME):
                clientGameController.repaintGameUI(packet,END_GAME,gameMonitor);
                roomController.showRoomsAfterEndGame(socketActionMonitor);
                break;

        }
    }

    // ОСНОВНОЙ ЦИКЛ
    public void connectedSocket(){
        isConnected = true;

        synchronized (connectionMonitor) {
            connectionMonitor.notify();
        }

        activateConnectionCheckTimer();

        while (isRunning) {
            Packet packet = udpSocket.receivePacketData();
            receiveCommand(packet);
        }
    }

    public void wrongUserInput(String command){
        if (connectionTryTimer != null){
            connectionTryTimer.cancel();
        }

        userInputProblem = command;
        isConnectionProblem = true;
        synchronized (connectionMonitor) {
            connectionMonitor.notify();
        }
    }

    public void activateConnectionTryTimer(){
        connectionTryTimer = new Timer();
        connectionTryTimer.schedule(new TimerTask() {
            @Override
            public  void run() {
                synchronized (connectionMonitor){
                    if (!isConnected()){
                        isConnectionProblem = true;
                        System.out.println("BAD CONNECTION");

                        connectionTryTimer.cancel();
                        connectionTryTimer = null;

                        connectionMonitor.notify();
                    }
                }

            }
        }, CONNECTION_CHECK_DELAY_MS);
    }

    public void activateConnectionCheckTimer(){
        Packet alivePacket = new Packet.Builder(ALIVE_CLIENT)
                .withClient(roomController.thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(alivePacket), mainServerIP, roomController.thisClient.serverPort);

        connectionCheckTimer = new Timer();
        connectionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (socketActionMonitor){
                    if (!isGetALIVE_SERVER){
                        isConnected = false;
                        System.out.println("BAD CONNECTION");

                        connectionCheckTimer.cancel();
                        connectionCheckTimer = null;

                        socketActionMonitor.notify();
                    }
                    else{
                        isGetALIVE_SERVER = false;
                        udpSocket.sendDataToIpPort(json.toJson(alivePacket), mainServerIP, roomController.thisClient.serverPort);
                        //activateConnectionCheckTimer();
                    }
                }
            }
        },CONNECTION_CHECK_DELAY_MS,CONNECTION_CHECK_DELAY_MS);
    }



    public void close() {
        System.out.println("DISCONNECTED");
        if (roomController.thisClient != null && roomController.thisClient.serverPort != null){
            Packet packet = new Packet.Builder(CLIENT_OFFLINE)
                    .withClient(roomController.thisClient)
                    .build();
            udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, roomController.thisClient.serverPort);

            roomController.thisClient = new Client();
        }
        roomController.rooms =  new ArrayList<>();

        isRunning = false;
    }
}

