package temp;

import Common.JsonSerialize;
import Common.Protocol.Client;
import Common.Protocol.Packet;
import Common.Protocol.Room;
import Common.UdpLibriary.UdpSocketExtension;

import javax.swing.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.*;

import static Common.Protocol.Protocol.*;


public class DUMP_UdpClientSocket extends Thread{
    private UdpSocketExtension udpSocket;

    public Client thisClient;
    public List<Room> rooms = new ArrayList<>();
    public Integer thisClientRoomId = null;

    public JButton connectButton;

    private String nickname;
    private InetAddress mainServerIP;
    private int mainServerPort;

    private boolean isRunning;
    private boolean isConnected;
    private boolean isConnectionProblem;
    public String userInputProblem;

    private JsonSerialize json = new JsonSerialize();

    private boolean isGetALIVE_SERVER = false;
    private Timer connectionTryTimer = null;
    private Timer connectionCheckTimer = null;
    private static final int CONNECTION_CHECK_DELAY_MS = 5000;

    public final Object connectionMonitor = new Object();
    public final Object socketActionMonitor = new Object();
    public String commandForGui;

    public DUMP_UdpClientSocket(String nickname, String IP, int port, JButton connectButton) throws SocketException, UnknownHostException {
        this.udpSocket = new UdpSocketExtension();

        this.nickname = nickname;
        this.mainServerIP = InetAddress.getByName(IP);
        this.mainServerPort = port;

        this.connectButton = connectButton;

        this.isConnected = false;
        this.isConnectionProblem = false;
        this.userInputProblem = null;
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
                System.out.println("SERVER ALIVE");
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
                thisClient = packet.client;
                if (!isConnected){
                    requestRooms();
                    connectedSocket();
                }
                break;

            case (RETURN_ROOMS):
                getRooms(packet);
                break;

            case (NO_ROOMS):
                noRooms();
                break;
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

    public void requestRooms(){
        Packet packet = new Packet.Builder(GET_ROOMS)
                .withClient(thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);
    }

    public void getRooms(Packet packet){
        commandForGui =GUI_REPAINT;
        if (packet.rooms != null){
            rooms = Arrays.asList(packet.rooms);
        }
        if (packet.msg != null){
            thisClientRoomId = Integer.parseInt(packet.msg);
            commandForGui =GUI_ROOM_REPAINT;
        }

        System.out.println(commandForGui);

        synchronized (socketActionMonitor) {
            socketActionMonitor.notify();
        }
    }

    public void noRooms(){
        rooms = new ArrayList<>();
        commandForGui =GUI_REPAINT;
        synchronized (socketActionMonitor) {
            socketActionMonitor.notify();
        }
    }

    public void requestCreateRoom(){
        Packet packet = new Packet.Builder(CREATE_ROOM)
                .withClient(thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);
    }

    public void requestJoinRoom(Room room){
        Packet packet = new Packet.Builder(JOIN_ROOM)
                .withClient(thisClient)
                .withRoom(room)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);
        thisClientRoomId = room.id;
    }

    public void requestDisconnectFromRoom(){
        Packet packet = new Packet.Builder(DISCONNECT_FROM_ROOM)
                .withClient(thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);
        thisClientRoomId = null;
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

                        connectionTryTimer = null;

                        connectionMonitor.notify();
                    }
                }

            }
        }, CONNECTION_CHECK_DELAY_MS);
    }

    public void activateConnectionCheckTimer(){
        Packet packet = new Packet.Builder(ALIVE_CLIENT)
                .withClient(thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);

        connectionCheckTimer = new Timer();
        connectionCheckTimer.schedule(new TimerTask() {
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
                        activateConnectionCheckTimer();
                    }
                }
            }
        },CONNECTION_CHECK_DELAY_MS);
    }



    public void close() {
        System.out.println("DISCONNECTED");
        if (thisClient != null && thisClient.serverPort != null){
            Packet packet = new Packet.Builder(CLIENT_OFFLINE)
                    .withClient(thisClient)
                    .build();
            udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);

            thisClient = new Client();
        }
        rooms =  new ArrayList<>();

        isRunning = false;
    }
}

