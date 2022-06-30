package Common.Protocol;

import com.alibaba.fastjson.JSON;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;

import static Common.Protocol.Protocol.ALIVE_CLIENT;

public class Client {
    public transient boolean isGetALIVE_CLIENT;

    public String nickname;
    public Integer id;
    private InetAddress IP;
    public Integer clientPort;
    public Integer serverPort;

//    public int roomId;

    public void setIP(String IP){
        try {
            this.IP = InetAddress.getByName(IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String getIP(){
        if (IP !=null){
            return IP.getHostAddress();
        }
        return null;
    }


    public void print(){
        String jsonString = JSON.toJSONString(this);
        System.out.println(jsonString);
    }

    public static int getClientListIndexByNickname(String nickname, List<Client> clients){
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).nickname.equals(nickname)){
                return i;
            }
        }
        return -1;
    }

    public static class Builder {
        private Client newClient;

        public Builder(String nickname) {
            newClient = new Client();
            newClient.nickname = nickname;
        }

        public Builder withId(int id){
            newClient.id = id;
            return this;
        }

        public Builder withIP_Port_CLIENT(InetAddress IP, int port){
            newClient.IP = IP;
            newClient.clientPort = port;
            return this;
        }

        public Builder withServerPort(int serverPort){
            newClient.serverPort = serverPort;
            return this;
        }

        public Client build(){
            return newClient;
        }

    }



//    public void printClient(){
//        System.out.println(nickname+id+clientIP+clientPort);
//    }

//    public static void main(String[] args) {
//        Client client = new Client("LOLS",1,"125.2.2.3",2);
//        //client.setNickname("LOL");
//
//        String jsonString = JSON.toJSONString(client);
//        System.out.println(jsonString);
//
//        Client newClient = JSON.parseObject(jsonString, Client.class);
//        newClient.printClient();
//    }
}
