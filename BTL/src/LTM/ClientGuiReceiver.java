package LTM;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientGuiReceiver extends JFrame {
    private JTextField txtUser;
    private JTextArea txtLog;
    private JButton btnConnect, btnReceive;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private List<String> emailList = new ArrayList<>();

    public ClientGuiReceiver(){
        setTitle("Client Người Nhận");
        setSize(550,450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header panel
        JPanel panel = new JPanel(new GridLayout(1,3,10,10));
        panel.setBorder(BorderFactory.createTitledBorder("Đăng ký người nhận"));
        panel.setBackground(new Color(224, 255, 255));

        panel.add(new JLabel("Tên người nhận:")); 
        txtUser = new JTextField(); panel.add(txtUser);

        btnConnect = new JButton("Kết nối Server");
        btnConnect.setBackground(new Color(100,149,237));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        panel.add(btnConnect);

        // Log email
        txtLog = new JTextArea(15,30); 
        txtLog.setFont(new Font("Courier New", Font.PLAIN, 14));
        txtLog.setBackground(new Color(240,248,255));
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Hộp thư đến"));

        // Nút nhận
        btnReceive = new JButton("Nhận email mới");
        btnReceive.setBackground(new Color(255,140,0));
        btnReceive.setForeground(Color.WHITE);
        btnReceive.setFocusPainted(false);

        add(panel, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);
        add(btnReceive, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> connectToServer());
        btnReceive.addActionListener(e -> showEmails());
    }

    private void connectToServer(){
        try{
            socket = new Socket("localhost",9999);
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            txtLog.append("Server: " + in.readLine() + "\n");
            JOptionPane.showMessageDialog(this,"Kết nối server thành công!");

            out.println("REGISTER|" + txtUser.getText());
            startListener(); // lắng nghe email mới
        }catch(IOException e){
            JOptionPane.showMessageDialog(this,"Không thể kết nối server: "+e.getMessage());
        }
    }

    private void startListener(){
        new Thread(() -> {
            try{
                String line;
                while((line=in.readLine())!=null){
                    if(line.startsWith("NEW_MAIL|")){
                        String[] parts = line.split("\\|");
                        String from = parts[1];
                        String subject = parts[2];
                        String content = parts[3].replace("\\n","\n");

                        String emailText = "Từ: " + from + "\nTiêu đề: " + subject + "\nNội dung:\n" + content + "\n--------------------\n";
                        synchronized(emailList){
                            emailList.add(emailText);
                        }
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }).start();
    }

    private void showEmails(){
        synchronized(emailList){
            if(emailList.isEmpty()){
                JOptionPane.showMessageDialog(this,"Chưa có email mới!");
            } else {
                txtLog.setText(""); // xóa log cũ
                for(String email: emailList){
                    txtLog.append(email);
                }
                txtLog.setCaretPosition(txtLog.getDocument().getLength()); // auto scroll
                emailList.clear();
            }
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new ClientGuiReceiver().setVisible(true));
    }
}
