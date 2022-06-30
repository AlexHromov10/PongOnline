package Client.GUI;

import Client.Game.Pong;
import Client.Socket.UdpClientSocket;
import Common.Protocol.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import static Common.Protocol.Protocol.*;

public class ClientWindow extends JFrame{
    private JPanel panel;
    private JTextField nicknameInput;
    private JTextField ipInput;
    private JTextField portInput;
    private JButton connectButton;
    private JPanel connectPanel;
    private JPanel loadingPanel;
    private JLabel loadingPanelLabel;
    private JPanel roomPanel;
    private JButton disconnectButton;
    private JPanel roomsSpace;
    private JButton createRoomButton;
    private JPanel roomSessionPanel;
    private JButton disconnectFromRoomButton;
    private JLabel roomPlayersLabel;
    private JLabel errorLabel;
    private JLabel countTimerLabel;

    private CardLayout cl = (CardLayout)panel.getLayout();

    public UdpClientSocket clientSocket;
    private Pong pongGame = null;

    private boolean inRoom = false;

    Timer timerBeforeGame = null;

    public ClientWindow() {
        this.getContentPane().add(panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new onExit());
        this.connectButton.addActionListener(new connectListener());
        this.disconnectButton.addActionListener(new disconnectListener());

        this.createRoomButton.addActionListener(new createRoomListener());
        this.disconnectFromRoomButton.addActionListener(new disconnectFromRoomListener());

        this.errorLabel.setForeground(Color.RED);
    }

    public void changeRoomLabel(){
        if (clientSocket.roomController.thisClientRoomId == null) {
            return;
        }

        if (pongGame != null) {
            return;
        }

        switch (clientSocket.roomController.rooms.get(Room.getRoomListIndexByRoomId(clientSocket.roomController.thisClientRoomId, clientSocket.roomController.rooms)).status) {
            case (ROOM_WAITING_PLAYER) -> {
                if (timerBeforeGame != null) {
                    cancelTimerBeforeGame();
                }
                if (pongGame != null) {
                    pongGame.setVisible(false);
                    pongGame.isGameRunning = false;
                }
                if (!this.isVisible()) {
                    this.setVisible(true);
                }
                roomPlayersLabel.setText("Waiting for player...");
            }
            case (ROOM_WAITING_START) -> {
                if (pongGame != null) {
                    pongGame.setVisible(false);
                    pongGame.isGameRunning = false;
                }
                if (!this.isVisible()) {
                    this.setVisible(true);
                }
                roomPlayersLabel.setText("Waiting for start!");
                clientSocket.roomController.requestStartPongGame();
                activateTimerBeforeGame();
            }
            case (ROOM_IN_GAME) -> roomPlayersLabel.setText("GAME IS ON!");
        }

    }

    public void activateTimerBeforeGame(){
        timerBeforeGame = new Timer();
        disconnectFromRoomButton.setEnabled(false);
        timerBeforeGame.scheduleAtFixedRate(new TimerTask() {
            int count = 3;
            @Override
            public void run() {
                roomPlayersLabel.setText("");
                countTimerLabel.setText(String.valueOf(count));
                if (count <= 1){
                    startGameSession();
                    cancelTimerBeforeGame();
                }
                else {
                    count--;
                }
            }
        },1000,1000);
    }

    public void startGameSession(){
        pongGame = new Pong(clientSocket);
        this.setVisible(false);
    }

    public void cancelTimerBeforeGame(){
        countTimerLabel.setText("");
        disconnectFromRoomButton.setEnabled(true);
        timerBeforeGame.cancel();
        timerBeforeGame = null;
    }

    private class onExit extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            if (clientSocket != null){
                clientSocket.close();
            }
            System.exit(0);
        }
    }

    public void setErrorLabelText(String text){
        errorLabel.setText(text);
        errorLabel.revalidate();
        errorLabel.repaint();
    }


    private class connectListener implements ActionListener {
        @Override
        public  void actionPerformed(ActionEvent actionEvent) {
            try {
                if (!isWalidIp(ipInput.getText())){
                    setErrorLabelText("Wrong IP address input!");
                    return;
                }
                if (!isWalidPort(portInput.getText())){
                    setErrorLabelText("Wrong Port input!");
                    return;
                }

                clientSocket = new UdpClientSocket(nicknameInput.getText(),ipInput.getText(),Integer.parseInt(portInput.getText()),connectButton);
                clientSocket.start();

                new connectionThread().start();

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        public boolean isWalidIp(String IP){
            try{
                InetAddress.getByName(IP);
            }
            catch (UnknownHostException e) {
                return false;
            }
            return true;
        }

        public boolean isWalidPort(String port){
            int portParsed;
            try{
                portParsed = Integer.parseInt(portInput.getText());
            }
            catch (NumberFormatException e) {
                return false;
            }
            if (portParsed<0 || portParsed>65535){
                return false;
            }

            return true;
        }

    }

    private class disconnectListener implements ActionListener {
        @Override
        public  void actionPerformed(ActionEvent actionEvent) {
            roomsSpace.removeAll();
            clientSocket.close();
            cl.show(panel,"connectPanelCard");
        }
    }


    private class createRoomListener implements ActionListener {
        @Override
        public  void actionPerformed(ActionEvent actionEvent) {
            inRoom=  true;
            clientSocket.roomController.requestCreateRoom();
            cl.show(panel,"roomSessionPanelCard");
            roomPlayersLabel.setText("Waiting for player...");
        }
    }

    private class disconnectFromRoomListener implements ActionListener {
        @Override
        public  void actionPerformed(ActionEvent actionEvent) {
            inRoom = false;
            clientSocket.roomController.requestDisconnectFromRoom();
            cl.show(panel,"roomPanelCard");
        }
    }


    public class connectionThread extends Thread{
        @Override
        public synchronized void run() {
            errorLabel.setText("");
            errorLabel.revalidate();
            errorLabel.repaint();

            cl.show(panel,"loadingPanelCard");
            loadingPanelLabel.setText("Connecting...");
            connect();

            waitSocketActions();
        }

        public void connect(){
            synchronized(clientSocket.connectionMonitor){
                 while(!clientSocket.isConnected() && !clientSocket.isConnectionProblem()) {
                    try {
                        clientSocket.connectionMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                paintRooms();
            }

            if (clientSocket.isConnected()){
                cl.show(panel,"roomPanelCard");
            }

            if (clientSocket.isConnectionProblem()){
                cl.show(panel,"loadingPanelCard");

                String showText = "ERROR! Unknown connection problem";

                if (clientSocket.userInputProblem != null){
                    switch (clientSocket.userInputProblem){
                        case (NICKNAME_IS_EMPTY):
                            showText = "ERROR! Nickname is empty! Enter nickname and try again";
                            break;
                        case (NICKNAME_IS_TAKEN):
                            showText = "ERROR! Nickname is taken! Enter another nickname and try again";
                            break;
                    }
                }
                else{
                    showText = "ERROR! Server not responding";
                }

                loadingPanelLabel.setText(showText);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cl.show(panel,"connectPanelCard");
            }
        }

        public void removeRoomsPaint(){
            roomsSpace.removeAll();
            roomsSpace.revalidate();
            roomsSpace.repaint();
        }


        public void paintRooms(){
            removeRoomsPaint();

            if (clientSocket.roomController.rooms.size()>0){
                for(Room room: clientSocket.roomController.rooms){
                    roomsSpace.add(new RoomPanel(room,clientSocket));
                }
            }
            else{
                roomsSpace.add(new JLabel("No rooms :("));
            }
            roomsSpace.revalidate();
            roomsSpace.repaint();
        }

        public void waitSocketActions(){
            synchronized(clientSocket.socketActionMonitor){
                while(clientSocket.isConnected()){
                    try {
                        clientSocket.socketActionMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    switch (clientSocket.roomController.commandForGui){
                        case (GUI_REPAINT):
                            System.out.println("inRoom: "+inRoom);
                            paintRooms();
                            break;

                        case (GUI_ROOM_REPAINT):
                            if (!inRoom){
                                cl.show(panel,"roomSessionPanelCard");
                            }
                            changeRoomLabel();
                            break;

                        case(END_GAME):
                            System.out.println("pongGame = null");
                            if (pongGame != null){
                                pongGame.setVisible(false);
                                pongGame.dispose();
                            }

                            pongGame = null;

                            //inRoom = false;
                            //clientSocket.roomController.requestDisconnectFromRoom();
                            //cl.show(panel,"roomPanelCard");
                            setVisible(true);
                            cl.show(panel,"roomSessionPanelCard");
                            changeRoomLabel();
                    }
                }

                if (!clientSocket.isConnected()){
                    cl.show(panel,"loadingPanelCard");
                    loadingPanelLabel.setText("Server not responding!");

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cl.show(panel,"connectPanelCard");
                }
            }

        }
    }
}
