package Client.Socket;

import Common.JsonSerialize;
import Common.Protocol.Client;
import Common.Protocol.GameBall;
import Common.Protocol.GamePaddle;
import Common.Protocol.Packet;
import Common.UdpLibriary.UdpSocketExtension;

import java.net.InetAddress;

import static Common.Protocol.Protocol.*;

public class ClientGameController {
    public boolean isGameStarted = false;

    public String commandForGameUI;

    private InetAddress mainServerIP;
    private JsonSerialize json = new JsonSerialize();
    private UdpSocketExtension udpSocket;

    public int playerNumberInGame;
    public int gameServerPort;

    public Packet receivedPacket;

    public ClientGameController(UdpSocketExtension udpSocket,InetAddress mainServerIP,int playerNumberInGame,int gameServerPort){
        this.udpSocket = udpSocket;
        this.mainServerIP = mainServerIP;
        this.playerNumberInGame = playerNumberInGame;
        this.gameServerPort = gameServerPort;
    }

    public void firstConnection(GamePaddle gamePaddle, Client client){
        System.out.println("First Connection");
        Packet packet = new Packet.Builder(CONFIRM_CONNECTION_TO_GAME)
                .withClient(client)
                .withGamePaddle(gamePaddle)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet),mainServerIP,gameServerPort);

        isGameStarted = true;
    }

    public void sendReadyForGame(Client client){
        Packet packet = new Packet.Builder(READY_FOR_GAME)
                .withClient(client)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet),mainServerIP,gameServerPort);
    }

    public void sendEndRound(boolean isRightWon){
        String command = RIGHT_WON;
        if (!isRightWon){
            command = LEFT_WON;
        }

        Packet packet = new Packet.Builder(command)
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet),mainServerIP,gameServerPort);
    }

    public void sendPaddleDirection(int yDir){
        GamePaddle gamePaddle = new GamePaddle.Builder()
                .withPNum(playerNumberInGame)
                .withYDir(yDir)
                .build();
        Packet packet = new Packet.Builder(SEND_PADDLE_Y_DIR)
                .withGamePaddle(gamePaddle)
                .build();

        udpSocket.sendDataToIpPort(json.toJson(packet),mainServerIP,gameServerPort);
    }

    public void sendDestination(double x, double y){
        Packet packet = new Packet.Builder(SEND_BALL_DESTINATION)
                .withGameBall(new GameBall(x,y))
                .build();
        udpSocket.sendDataToIpPort(json.toJson(packet),mainServerIP,gameServerPort);
    }

    public void repaintGameUI(Packet packet,String commandForGameUI, Object gameMonitor){
        receivedPacket = packet;
        this.commandForGameUI = commandForGameUI;
        synchronized (gameMonitor) {
            gameMonitor.notify();
        }
    }
}
