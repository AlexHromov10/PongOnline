package Server;

import Common.Protocol.Client;
import Common.Protocol.GamePaddle;
import Common.Protocol.Packet;
import Common.Protocol.Room;
import Common.UdpLibriary.UdpSocketExtension;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static Common.Protocol.Protocol.*;

public class UdpServerSocket{
    public static final int MAIN_SERVER_PORT = 25565;

    UdpSocketExtension udpSocket;
    List<Client> clients = new ArrayList<>();
    List<Room> rooms = new ArrayList<>();

    int clientsFreeIDs = 0;
    int roomsFreeIDs = 0;
    int freePort =25566;

    boolean isRunning;

    public UdpServerSocket() throws SocketException {
        udpSocket = new UdpSocketExtension(MAIN_SERVER_PORT);
    }

    public void run(){
        isRunning = true;

        while (isRunning){
            receiveCommand(udpSocket.receivePacketData());
        }
    }

    public void receiveCommand(Packet packet){
        switch (packet.command){
            case (ALIVE_CLIENT):
                if (Client.getClientListIndexByNickname(packet.client.nickname,clients) != -1) {
                    clients.get(Client.getClientListIndexByNickname(packet.client.nickname, clients)).isGetALIVE_CLIENT = true;
                    Packet sendPacket = new Packet.Builder(ALIVE_SERVER)
                            .build();
                    udpSocket.sendDataToClient(sendPacket, packet.client);
                }
                break;

            case (CLIENT_OFFLINE):
                removeClientFromMainServer(packet.client,false);
                break;

            case (UNKNOWN_ERROR):
                System.out.println("ERROR: "+packet.command);
                packet.client.print();
                break;

            case (CLIENT_CONNECT_MSG):
                connectNewClient(packet);
                break;

            case (GET_ROOMS):
                returnRooms(packet.client);
                break;

            case (CREATE_ROOM):
                createRoom(packet.client);
                break;

            case(DISCONNECT_FROM_ROOM):
                int roomId = getRoomIdWithClient(packet.client);
                removePlayerFromRoom(roomId,packet.client);
                break;

            case(JOIN_ROOM):
                joinRoom(packet.rooms[0],packet.client);
                System.out.println("JOIN ROOM: "+ packet.rooms[0].id);
                break;

            case(REQUEST_START_GAME):
                startGameSocket(packet.client);
                break;

            default:
                System.out.println("NOT AUTHORIZED: "+packet.command);
                udpSocket.sendDataBackToReceived(new Packet.Builder(NOT_AUTH).build());
                break;
        }
    }

    public void activateClientConnectionCheckTimer(Client client){
        Timer connectionTimer = new Timer();
        connectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(clients.size() >0) {
                    if(Client.getClientListIndexByNickname(client.nickname, clients) != -1){
                        if (clients.get(Client.getClientListIndexByNickname(client.nickname, clients)).isGetALIVE_CLIENT == false) {
                            removeClientFromMainServer(client, true);
                            connectionTimer.cancel();
                        } else {
                            clients.get(Client.getClientListIndexByNickname(client.nickname, clients)).isGetALIVE_CLIENT = false;
                        }
                    }
                }
            }
        },7000, 7000);
    }

    public void connectNewClient(Packet packet){
        if (isEmptyNickname(packet) || isTakenNickname(packet) || isMaxClientsOnServer(packet)) {
            return;
        }

        System.out.println("Player '"+ packet.client.nickname +"' connected!");

        addClientToMainServer(packet.client.nickname,udpSocket.receivedPacket.getAddress(),udpSocket.receivedPacket.getPort());

        activateClientConnectionCheckTimer(clients.get(Client.getClientListIndexByNickname(packet.client.nickname,clients)));

        Packet sendPacket = new Packet.Builder(SUCCESS_CONNECT)
                .withClient(clients.get(Client.getClientListIndexByNickname(packet.client.nickname,clients)))
                .build();

        udpSocket.sendDataToClient(sendPacket,sendPacket.client);

        //connectPlayer(clients[IDs]);
        //freePort++;
    }

    public boolean isEmptyNickname(Packet packet){
        if (packet.client.nickname.isEmpty()){
            packet.command = NICKNAME_IS_EMPTY;
            udpSocket.sendDataBackToReceived(packet);
            System.out.println("empty nickname");
            return true;
        }
        return false;
    }

    public boolean isTakenNickname(Packet packet){
        for(Client client : clients){
            if (client.nickname.equals(packet.client.nickname)){
                packet.command = NICKNAME_IS_TAKEN;
                udpSocket.sendDataBackToReceived(packet);
                System.out.println("taken nickname");
                return true;
            }
        }
        return false;
    }

    public boolean isMaxClientsOnServer(Packet packet){
        if (clients.size() >= 20){
            packet.command = MAX_CLIENTS;
            udpSocket.sendDataBackToReceived(packet);
            System.out.println("maximum clients");
            return true;
        }
        return false;
    }

    public void addClientToMainServer(String nickname, InetAddress IP, int port){
        clients.add(new Client.Builder(nickname)
                .withId(clientsFreeIDs)
                .withIP_Port_CLIENT(IP,port)
                .withServerPort(MAIN_SERVER_PORT)
                .build()
        );
        clients.get(Client.getClientListIndexByNickname(nickname,clients)).isGetALIVE_CLIENT = true;
        clientsFreeIDs++;
    }

    public synchronized void removeClientFromMainServer(Client client, boolean lostConnection){
        int clientListId = Client.getClientListIndexByNickname(client.nickname,clients);

        if (clientListId != -1){
            clients.remove(clientListId);
            if (lostConnection){
                System.out.println("Player '"+ client.nickname +"' lost connection!!!");
            }
            else{
                System.out.println("Player '"+ client.nickname +"' disconnected!");
            }
        }
        else {
            return;
        }

        int roomIdWithDisconnectedClient=getRoomIdWithClient(client);
        if (roomIdWithDisconnectedClient != -1){
            removePlayerFromRoom(roomIdWithDisconnectedClient,client);
        }
    }

    public void startGameSocket(Client client) {
        try {
            int roomId = Room.getRoomListIndexByRoomId(getRoomIdWithClient(client),rooms);
            int playerPaddleNumber;

            if (rooms.get(roomId).roomSocket != null) {
                if (rooms.get(roomId).roomSocket.isConnectionRunning == false) {
                    rooms.get(roomId).roomSocket = null;
                }
            }

            if (rooms.get(roomId).roomSocket == null){
                rooms.get(roomId).roomSocket = new UdpClientConnectionHandler(freePort);
                freePort++;
            }


            if (rooms.get(roomId).roomSocket.client1 == null){
                playerPaddleNumber = 1;
                rooms.get(roomId).roomSocket.start();
                rooms.get(roomId).roomSocket.client1 = client;
            }
            else{
                playerPaddleNumber = 2;
                rooms.get(roomId).roomSocket.client2 = client;
            }

//            if (rooms.get(roomId).roomSocket == null){
//                playerPaddleNumber = 1;
//                rooms.get(roomId).roomSocket = new UdpClientConnectionHandler(freePort);
//                rooms.get(roomId).roomSocket.start();
//                rooms.get(roomId).roomSocket.client1 = client;
//                freePort++;
//            }
//            else{
//                playerPaddleNumber = 2;
//                rooms.get(roomId).roomSocket.client2 = client;
//            }

            GamePaddle gamePaddle = new GamePaddle.Builder()
                    .withPNum(playerPaddleNumber)
                    .build();
            Packet packet = new Packet.Builder(CONNECT_TO_A_NEW_SOCKET)
                    .withMsg(String.valueOf(rooms.get(roomId).roomSocket.PORT))
                    .withGamePaddle(gamePaddle)
                    .build();

            udpSocket.sendDataToClient(packet,client);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////
    // ROOMS
    public void returnRooms(Client client){
        Packet sendPacket = new Packet.Builder(NO_ROOMS)
                .build();

        int roomId = getRoomIdWithClient(client);

        if (rooms.size() != 0){
            sendPacket.command = RETURN_ROOMS;
            sendPacket.rooms = rooms.toArray(new Room[rooms.size()]);
            if (roomId != -1){
                sendPacket.msg = String.valueOf(roomId);
            }
        }
        udpSocket.sendDataToClient(sendPacket,client);
    }

    public void sendRoomsToAllClients(){
        for(Client client:clients){
            returnRooms(client);
        }
    }

    public void createRoom(Client client){
        rooms.add(new Room(roomsFreeIDs,client,ROOM_WAITING_PLAYER));

        // ЭТО ВАРИАНТ ДЛЯ СОХРАНЕНИЯ НОМЕРА КОМНАТЫ У ЮЗЕРА.
        // Но! Придется менять логику с RETURN_ROOMS+msg, т.к. msg больше не будет
        //clients.get(Client.getClientListIndexByNickname(client.nickname,clients)).roomId = roomsFreeIDs;

        sendRoomsToAllClients();

        roomsFreeIDs++;
    }

    public void joinRoom(Room room, Client client){
        rooms.get(Room.getRoomListIndexByRoomId(room.id,rooms)).addClient(client);
        rooms.get(Room.getRoomListIndexByRoomId(room.id,rooms)).status = ROOM_WAITING_START;

        sendRoomsToAllClients();
    }

    public void removePlayerFromRoom(int roomId, Client client){
        int roomListId = Room.getRoomListIndexByRoomId(roomId,rooms);
        if (roomListId != -1) {
            rooms.get(roomListId).removeClientFromRoom(client);
            rooms.get(roomListId).status = ROOM_WAITING_PLAYER;


            if (rooms.get(roomListId).roomSocket != null){
                rooms.get(roomListId).roomSocket.sendEndGame();
                System.out.println("NULLED SOCKET");
                rooms.get(roomListId).roomSocket = null;
            }

            if (rooms.get(Room.getRoomListIndexByRoomId(roomId, rooms)).isEmpty) {
                deleteRoom(roomId);
            }
        }



        sendRoomsToAllClients();
    }

    public int getRoomIdWithClient(Client clientToFind){
        int roomId = -1;
        for (Room room:rooms) {
            for (int i = 0; i < room.clientsInRoom.length; i++) {
                if (room.clientsInRoom[i]!= null && room.clientsInRoom[i].nickname.equals(clientToFind.nickname)){
                    roomId=room.id;
                    break;
                }
            }
        }
        return roomId;
    }

    public void deleteRoom(int roomId){
        System.out.println("DELETED ROOM №"+roomId);
        rooms.remove(rooms.get(Room.getRoomListIndexByRoomId(roomId,rooms)));
    }



}

