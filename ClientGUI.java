import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientGUI {

    JFrame loginFrame = new JFrame("Login / Register");

    JFrame chatFrame = new JFrame("Chat App");

    JTextArea chatArea = new JTextArea();

    JTextField messageField = new JTextField();

    JButton sendButton = new JButton("Send");

    JTextField usernameField = new JTextField();

    JPasswordField passwordField = new JPasswordField();
    
    JButton loginButton = new JButton("Login");

    JButton registerButton = new JButton("Register");

    DataInputStream dis;
    DataOutputStream dos;

    String username;

    public ClientGUI() {  
        showLoginGUI();
    }

    public void showLoginGUI() {

        loginFrame.setSize(350, 250);

        loginFrame.setLayout(new GridLayout(5, 1, 10, 10));

        JPanel userPanel = new JPanel(new BorderLayout());

        userPanel.add(new JLabel("Username "), BorderLayout.WEST);

        userPanel.add(usernameField, BorderLayout.CENTER);

        JPanel passPanel = new JPanel(new BorderLayout());

        passPanel.add(new JLabel("Password "), BorderLayout.WEST);

        passPanel.add(passwordField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(loginButton);

        buttonPanel.add(registerButton);

        loginFrame.add(new JLabel("Chat Application", SwingConstants.CENTER));

        loginFrame.add(userPanel);

        loginFrame.add(passPanel);

        loginFrame.add(buttonPanel);

        loginButton.addActionListener(e -> login());

        registerButton.addActionListener(e -> register());

        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginFrame.setLocationRelativeTo(null);

        loginFrame.setVisible(true);
    }

    public void login() {

        username = usernameField.getText();

        String password = new String(passwordField.getPassword());

        if (authenticate(username, password)) {

            JOptionPane.showMessageDialog(loginFrame, "Login Successful");

            loginFrame.dispose();

            connectToServer();

            showChatGUI();

        } else {

            JOptionPane.showMessageDialog(loginFrame, "Invalid Username or Password");
        }
    }

    public void register() {

        String username = usernameField.getText();

        String password = new String(passwordField.getPassword());

        try {

            FileWriter fw = new FileWriter("users.txt", true);

            fw.write(username + "," + password + "\n");

            fw.close();

            JOptionPane.showMessageDialog(loginFrame, "Registration Successful");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public boolean authenticate(String username, String password) {

        try {

            BufferedReader br = new BufferedReader(new FileReader("users.txt"));

            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");

                if (parts[0].equals(username) && parts[1].equals(password)) {

                    br.close();

                    return true;
                }
            }

            br.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return false;
    }

    public void connectToServer() {

        try {

            String serverIP = JOptionPane.showInputDialog(
                    chatFrame,
                    "Enter Server IP",
                    "localhost"
            );

            Socket socket = new Socket(serverIP, 5000);

            dis = new DataInputStream(socket.getInputStream());

            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(username);

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    chatFrame,
                    "Connection Failed"
            );

            System.exit(0);
        }
    }

    public void showChatGUI() {

        chatFrame.setSize(500, 500);

        chatFrame.setLayout(new BorderLayout());

        chatArea.setEditable(false);

        chatFrame.add(new JScrollPane(chatArea),
                BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(
                new BorderLayout()
        );

        bottomPanel.add(messageField,
                BorderLayout.CENTER);

        bottomPanel.add(sendButton,
                BorderLayout.EAST);

        chatFrame.add(bottomPanel,
                BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());

        messageField.addActionListener(e -> sendMessage());

        chatFrame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        chatFrame.setLocationRelativeTo(null);

        chatFrame.setVisible(true);

        receiveMessages();
    }

    public void sendMessage() {

        try {

            String msg = messageField.getText();

            dos.writeUTF(msg);

            messageField.setText("");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void receiveMessages() {

        Thread t = new Thread(() -> {

            while (true) {

                try {

                    String msg = dis.readUTF();

                    chatArea.append(msg + "\n");

                } catch (IOException e) {

                    break;
                }
            }
        });

        t.start();
    }

    public static void main(String[] args) {

        new ClientGUI();
    }
}