package LTM;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SMTPServer {
    private static final int PORT = 9999;
    private static Map<String, PrintWriter> onlineClients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server đang chạy trên cổng " + PORT);

        File emailDir = new File("emails");
        if (!emailDir.exists()) emailDir.mkdir();

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(new ClientHandler(client)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username = "";

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("220 Simple SMTP Server Ready");

                String line;
                while ((line = in.readLine()) != null){
                    if(line.startsWith("REGISTER|")) {
                        username = line.split("\\|")[1].trim();
                        onlineClients.put(username, out);
                        System.out.println(username + " đã đăng ký nhận email.");
                        continue;
                    }

                    if(line.startsWith("SEND|")) {
                        // SEND|from|to|subject|content
                        String[] parts = line.split("\\|",5);
                        String from = parts[1];
                        String to = parts[2];
                        String subject = parts[3];
                        String content = parts[4];

                        // Lưu file email
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        File file = new File("emails/email_" + timestamp + ".txt");
                        try(PrintWriter pw = new PrintWriter(file)){
                            pw.println("From: " + from);
                            pw.println("To: " + to);
                            pw.println("Subject: " + subject);
                            pw.println("Date: " + new Date());
                            pw.println();
                            pw.println(content);
                        }

                        // Push notification nếu người nhận online
                        if(onlineClients.containsKey(to)){
                            PrintWriter receiverOut = onlineClients.get(to);
                            receiverOut.println("NEW_MAIL|" + from + "|" + subject + "|" + content.replace("\n","\\n"));
                        }

                        out.println("250 Email sent");
                        continue;
                    }

                    if(line.equals("QUIT")){
                        out.println("221 Bye");
                        break;
                    }
                }

                if(!username.isEmpty()){
                    onlineClients.remove(username);
                    System.out.println(username + " đã ngắt kết nối.");
                }
                socket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
