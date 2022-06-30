package Server;

import java.net.SocketException;

public class ServerUDP {
    public static void main(String[] args){
        try {
            UdpServerSocket serverSocket = new UdpServerSocket();
            serverSocket.run();
        } catch (SocketException e) {
            e.printStackTrace();
        }

//        try {
//            serverSocket = new UdpServerSocket();
//            serverSocket.start();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
    }


}
