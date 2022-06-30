package Client.GUI;

import Client.GUI.ClientWindow;

import java.awt.*;

public class ClientMain {
    public static void main(String[] args) {
        ClientWindow clientWindow = new ClientWindow();
        clientWindow.pack();
        clientWindow.setSize(new Dimension(700, 300));
        clientWindow.setVisible(true);
    }
}
