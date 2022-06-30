package Client.GUI;

import Client.Socket.UdpClientSocket;
import Common.Protocol.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static Common.Protocol.Protocol.*;


public class RoomPanel extends JPanel {
    private static final int GAP = 20;

    private JButton roomButton = new JButton("connect");
    JLabel centerLabel;

    Room room;
    UdpClientSocket clientSocket;

    public RoomPanel(Room room,UdpClientSocket clientSocket) {
        this.room = room;
        setOpaque(true);
        switch (room.status){
            case(ROOM_WAITING_PLAYER):
                setBackground(Color.GREEN);
                break;

            case(ROOM_WAITING_START):
                setBackground(Color.GRAY);
                break;

            case(ROOM_IN_GAME):
                setBackground(Color.RED);
                break;
        }
        setLayout(new GridBagLayout());


        centerLabel = new JLabel(room.getRoomTitle(), JLabel.CENTER);
        if (room.isMax){
            roomButton.setEnabled(false);
        }
        else{
            roomButton.setEnabled(true);
        }
        roomButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(centerLabel);
        add(roomButton);

        this.roomButton.addActionListener(new joinButtonListener());
        this.clientSocket = clientSocket;


    }

    private class joinButtonListener implements ActionListener {
        @Override
        public  void actionPerformed(ActionEvent actionEvent) {
            //System.out.println("join room №"+room.id);
            clientSocket.roomController.requestJoinRoom(room);
        }
    }

}
