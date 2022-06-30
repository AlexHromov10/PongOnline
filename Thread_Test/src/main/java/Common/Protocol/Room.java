package Common.Protocol;

import Server.UdpClientConnectionHandler;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private static int MAX_PLAYERS = 2;
    public transient UdpClientConnectionHandler roomSocket = null;

    public int id;
    public Client[] clientsInRoom;
    public String status;

    public boolean isEmpty;
    public boolean isMax;

    public Room(int id, Client clientCreatedRoom, String status){
        this.id = id;
        clientsInRoom = new Client[MAX_PLAYERS];
        clientsInRoom[0] = clientCreatedRoom;
        this.status = status;
        this.isEmpty = false;
        this.isMax = false;
    }

    public String getRoomTitle(){
        String title="";
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (clientsInRoom[i] != null){
                title += "'"+clientsInRoom[i].nickname+"' ";
                if (i != getAmountOfPlayers()-1){
                    title +=", ";
                }
            }
        }
        title+=" room";
        return title;
    }

    public int getAmountOfPlayers(){
        int count = 0;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (clientsInRoom[i] != null){
                count++;
            }
        }
        return count;
    }

    public void addClient(Client client){
        if (!isMaxPlayersInRoom()){
            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (clientsInRoom[i] == null){
                    clientsInRoom[i] = client;
                }
            }
        }
        isMaxPlayersInRoom();
    }

    public void removeClientFromRoom(Client client){
        if (!isEmptyRoom()){
            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (clientsInRoom[i] != null && clientsInRoom[i].nickname.equals(client.nickname)){
                    clientsInRoom[i] = null;
                }
            }
        }
        isMaxPlayersInRoom();
        isEmptyRoom();
    }

    public boolean isEmptyRoom(){
        int count = 0;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (clientsInRoom[i] == null){
                count++;
            }
        }
        if (count == MAX_PLAYERS){
            isEmpty = true;
            return true;
        }
        return false;
    }

    public boolean isMaxPlayersInRoom(){
        if (getAmountOfPlayers() == MAX_PLAYERS){
            isMax = true;
            return true;
        }
        else{
            isMax = false;
            return false;
        }

    }

    public static int getRoomListIndexByRoomId(int id, List<Room> rooms){
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).id == id){
                return i;
            }
        }
        return -1;
    }


}
