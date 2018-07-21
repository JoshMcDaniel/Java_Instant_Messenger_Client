import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Client extends JFrame{

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message;
    private String serverIP;
    private Socket connection;

    //constructor
    public Client(String host) {
        super("Josh's Instant Messenger Client");
        serverIP = host;
        userText = new JTextField();
        userText.setEnabled(false);
        userText.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(400,200);
        setVisible(true);
    }

    //connect to server
    public void startRunning() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("\n Client terminated the connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    //connect to server
    private void connectToServer() throws IOException {
        showMessage("Attempting to connect...\n");
        connection = new Socket(InetAddress.getByName(serverIP), 6789);
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    //set up streams to send and receive messages
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n You are now connected \n");
    }

    //while chatting with server
    private void whileChatting() throws IOException {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\n The received Object type is not correct");
            }
        } while (!message.equals("SERVER - END"));
    }

    //close streams and sockets
    private void closeConnection() {
        showMessage("\n Closing the connection...");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ioExcetion) {
            ioExcetion.printStackTrace();
        }
    }

    //send messages to server
    private void sendMessage(String message) {
        try {
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        } catch (IOException ioExcetion) {
            chatWindow.append("\n ERROR sending message");
        }
    }

    //change or update chat window
    private void showMessage(final String message) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        chatWindow.append(message);
                    }
                }
        );
    }

    //gives user permission to type into textbox
    private void ableToType(final boolean tof) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        userText.setEnabled(tof);
                    }
                }
        );
    }

}
