package Client.Socket;

import Common.JsonSerialize;
import Common.Protocol.Client;
import Common.Protocol.Packet;
import Common.Protocol.Room;
import Common.UdpLibriary.UdpSocketExtension;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Common.Protocol.Protocol.*;

public class ClientRoomController {
    private UdpSocketExtension udpSocket;
    private InetAddress mainServerIP;
    private JsonSerialize json = new JsonSerialize();

    public Client thisClient;
    public List<Room> rooms = new ArrayList<>();
    public Integer thisClientRoomId = null;
    public String commandForGui;

    public ClientRoomController(UdpSocketExtension udpSocket, InetAddress mainServerIP){
        this.udpSocket = udpSocket;
        this.mainServerIP = mainServerIP;
    }


    public void requestRooms(){
        Packet packet = new Packet.Builder(GET_ROOMS)
                .withClient(thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);
    }

    public void getRooms(Packet packet, Object socketActionMonitor){
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

    public void noRooms( Object socketActionMonitor){
        rooms = new ArrayList<>();
        commandForGui =GUI_REPAINT;
        synchronized (socketActionMonitor) {
            socketActionMonitor.notify();
        }
    }

    public void showRoomsAfterEndGame(Object socketActionMonitor){
        commandForGui =END_GAME;
        synchronized (socketActionMonitor) {
            socketActionMonitor.notify();
        }
        //requestRooms();
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

    public void requestStartPongGame(){
        Packet packet = new Packet.Builder(REQUEST_START_GAME)
                .withClient(thisClient)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet), mainServerIP, thisClient.serverPort);
    }
}
