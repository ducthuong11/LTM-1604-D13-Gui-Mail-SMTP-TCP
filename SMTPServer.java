package LTM;

import java.io.*;
import java.net.*;
import java.util.*;

// Lớp Message để lưu trữ tin nhắn
class Message implements Serializable {
    String from;
    String to;
    String subject;
    String content;
    Date date;

    public Message(String from, String to, String subject, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.date = new Date();
    }

    @Override
    public String toString() {
        return "📩 Từ: " + from + " ➝ " + to + " | Chủ đề: " + subject + " | " + date;
    }
}

// SERVER
public class SMTPServer {
    private static final int PORT = 5000;
    private static Map<String, List<Message>> inbox = new HashMap<>();
    private static Map<String, List<Message>> sent = new HashMap<>();
    private static final String SAVE_FILE = "messages.dat";

    public static void main(String[] args) throws IOException {
        loadData();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("📡 Mail Server đang chạy tại cổng " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                while (true) {
                    String command = (String) in.readObject();

                    if (command.equals("SEND")) {
                        Message msg = (Message) in.readObject();
                        inbox.computeIfAbsent(msg.to, k -> new ArrayList<>()).add(msg);
                        sent.computeIfAbsent(msg.from, k -> new ArrayList<>()).add(msg);
                        saveData();
                    } else if (command.equals("INBOX")) {
                        String user = (String) in.readObject();
                        out.writeObject(inbox.getOrDefault(user, new ArrayList<>()));
                    } else if (command.equals("SENT")) {
                        String user = (String) in.readObject();
                        out.writeObject(sent.getOrDefault(user, new ArrayList<>()));
                    } else if (command.equals("DELETE")) {
                        String box = (String) in.readObject();
                        String user = (String) in.readObject();
                        int index = (int) in.readObject();

                        if (box.equals("INBOX")) {
                            inbox.getOrDefault(user, new ArrayList<>()).remove(index);
                        } else {
                            sent.getOrDefault(user, new ArrayList<>()).remove(index);
                        }
                        saveData();
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Client ngắt kết nối");
            }
        }
    }

    private static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(inbox);
            out.writeObject(sent);
        } catch (Exception ignored) {}
    }

    private static void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            inbox = (Map<String, List<Message>>) in.readObject();
            sent = (Map<String, List<Message>>) in.readObject();
        } catch (Exception ignored) {}
    }
}
