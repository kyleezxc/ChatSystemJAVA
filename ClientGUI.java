import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientGUI {

    JFrame loginFrame = new JFrame("Login / Register");
    JFrame chatFrame = new JFrame("Chat Application");

    JTextArea chatArea = new JTextArea();
    JTextField messageField = new JTextField();

    JButton sendButton = new JButton("Send");
    JButton historyButton = new JButton("Show History");

    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();

    JButton loginButton = new JButton("Login");
    JButton registerButton = new JButton("Register");

    DataInputStream dis;
    DataOutputStream dos;

    String username;

    
    final String HISTORY_FILE = "chat_history.txt";

    public ClientGUI() {
        showLoginGUI();
    }

    public void showLoginGUI() {

        loginFrame.setSize(400, 300);
        loginFrame.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Chat Application", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(new JLabel("Username: "), BorderLayout.WEST);
        userPanel.add(usernameField, BorderLayout.CENTER);

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.add(new JLabel("Password: "), BorderLayout.WEST);
        passPanel.add(passwordField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        formPanel.add(userPanel);
        formPanel.add(passPanel);
        formPanel.add(buttonPanel);

        loginFrame.add(title, BorderLayout.NORTH);
        loginFrame.add(formPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    
    public void login() {

        username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (authenticate(username, password)) {

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "Login Successful!"
            );

            loginFrame.dispose();

            connectToServer();

            showChatGUI();

        } else {

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "Invalid Username or Password"
            );
        }
    }


    public void register() {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "Username and Password cannot be empty."
            );

            return;
        }

        try {

            FileWriter fw = new FileWriter("users.txt", true);

            fw.write(username + "," + password + "\n");

            fw.close();

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "Registration Successful!"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    public boolean authenticate(String username, String password) {

        try {

            BufferedReader br = new BufferedReader(
                    new FileReader("users.txt")
            );

            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");

                if (parts[0].equals(username)
                        && parts[1].equals(password)) {

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
                    "Connection Failed!"
            );

            System.exit(0);
        }
    }

    public void showChatGUI() {

        chatFrame.setSize(650, 500);
        chatFrame.setLayout(new BorderLayout(10, 10));

       
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(chatArea);

       
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel welcomeLabel = new JLabel(" Welcome, " + username);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(historyButton, BorderLayout.EAST);

        
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        chatFrame.add(topPanel, BorderLayout.NORTH);
        chatFrame.add(scrollPane, BorderLayout.CENTER);
        chatFrame.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());

        messageField.addActionListener(e -> sendMessage());

        historyButton.addActionListener(e -> showHistory());

        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatFrame.setLocationRelativeTo(null);

        chatFrame.setVisible(true);

        receiveMessages();
    }


    public void sendMessage() {

        try {

            String msg = messageField.getText().trim();

            if (msg.isEmpty()) {
                return;
            }

            dos.writeUTF(msg);

            String formattedMessage =
                    timestamp() + " You: " + msg;

            chatArea.append(formattedMessage + "\n");

            saveHistory(formattedMessage);

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

                    String formattedMessage =
                            timestamp() + " " + msg;

                    chatArea.append(formattedMessage + "\n");

                    saveHistory(formattedMessage);

                } catch (IOException e) {

                    chatArea.append("Disconnected from server.\n");

                    break;
                }
            }
        });

        t.start();
    }

    public void saveHistory(String message) {

        try {

            FileWriter fw = new FileWriter(HISTORY_FILE, true);

            fw.write(message + "\n");

            fw.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void showHistory() {

        try {

            BufferedReader br = new BufferedReader(
                    new FileReader(HISTORY_FILE)
            );

            JTextArea historyArea = new JTextArea(20, 40);

            historyArea.setEditable(false);

            String line;

            while ((line = br.readLine()) != null) {

                historyArea.append(line + "\n");
            }

            br.close();

            JScrollPane scrollPane =
                    new JScrollPane(historyArea);

            JOptionPane.showMessageDialog(
                    chatFrame,
                    scrollPane,
                    "Chat History",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    chatFrame,
                    "No chat history found."
            );
        }
    }

    public String timestamp() {

        return "[" +
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "HH:mm:ss"
                        )
                ) +
                "]";
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new ClientGUI());
    }
}
