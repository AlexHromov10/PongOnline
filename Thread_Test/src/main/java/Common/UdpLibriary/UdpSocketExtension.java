package Common.UdpLibriary;

import Common.Protocol.Client;
import Common.JsonSerialize;
import Common.Protocol.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpSocketExtension extends DatagramSocket {
    private int BUFFER_SIZE = 2048;
    private byte[] buffer = new byte[BUFFER_SIZE];

    public DatagramPacket receivedPacket = new DatagramPacket(buffer,buffer.length);
    public DatagramPacket sendPacket;

    JsonSerialize json = new JsonSerialize();

    public UdpSocketExtension() throws SocketException {
        super();
    }

    public UdpSocketExtension(int port) throws SocketException {
        super(port);
    }

    public Packet receivePacketData(){
        receiveIt();

        String received  = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
        System.out.println(received);// DELETE
        return json.fromJson(received,Packet.class);
    }

    public void sendDataToIpPort(String msg, InetAddress IP, int port){
        buffer = msg.getBytes(StandardCharsets.UTF_8);
        sendPacket = new DatagramPacket(buffer,buffer.length,IP, port);

        sendIt();
    }

    public void sendDataBackToReceived(Packet packet){
        String jsonString = json.toJson(packet);
        buffer = jsonString.getBytes(StandardCharsets.UTF_8);

        sendPacket = new DatagramPacket(buffer,buffer.length,receivedPacket.getAddress(), receivedPacket.getPort());

        sendIt();
    }

    public void sendDataToClient(Packet packet, Client client){
        String jsonString = json.toJson(packet);
        buffer = jsonString.getBytes(StandardCharsets.UTF_8);

        try {
            sendPacket = new DatagramPacket(buffer,buffer.length, InetAddress.getByName(client.getIP()), client.clientPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        sendIt();
    }

    private void receiveIt(){
        try {
            receive(receivedPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendIt(){
        try {
            send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
