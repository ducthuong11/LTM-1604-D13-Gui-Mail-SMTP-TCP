package LTM;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientGuiSenderTCP_Fancy extends JFrame {
    private JTextField txtFrom, txtTo, txtSubject;
    private JTextArea txtContent, txtLog;
    private JButton btnConnect, btnSend;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientGuiSenderTCP_Fancy(){
        setTitle("Client Người Gửi");
        setSize(550,550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header panel
        JPanel panel = new JPanel(new GridLayout(4,2,10,10));
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin Email"));
        panel.setBackground(new Color(224, 255, 255));

        panel.add(new JLabel("Người gửi:")); 
        txtFrom = new JTextField(); panel.add(txtFrom);

        panel.add(new JLabel("Người nhận:")); 
        txtTo = new JTextField(); panel.add(txtTo);

        panel.add(new JLabel("Tiêu đề:")); 
        txtSubject = new JTextField(); panel.add(txtSubject);

        btnConnect = new JButton("Kết nối Server"); 
        btnConnect.setBackground(new Color(100,149,237));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        panel.add(btnConnect);

        btnSend = new JButton("Gửi Email"); 
        btnSend.setBackground(new Color(60,179,113));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        panel.add(btnSend);

        // Nội dung email
        txtContent = new JTextArea(10,30);
        txtContent.setLineWrap(true); txtContent.setWrapStyleWord(true);
        txtContent.setFont(new Font("Arial", Font.PLAIN, 14));
        txtContent.setBackground(new Color(245,245,245));
        JScrollPane scrollContent = new JScrollPane(txtContent);
        scrollContent.setBorder(BorderFactory.createTitledBorder("Nội dung Email"));

        // Log
        txtLog = new JTextArea(10,30); 
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Courier New", Font.PLAIN, 13));
        txtLog.setBackground(new Color(230,230,250));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log"));

        add(panel, BorderLayout.NORTH);
        add(scrollContent, BorderLayout.CENTER);
        add(scrollLog, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> connectToServer());
        btnSend.addActionListener(e -> sendEmail());
    }

    private void connectToServer(){
        try{
            socket = new Socket("localhost",9999);
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            txtLog.append("Server: " + in.readLine() + "\n");
            JOptionPane.showMessageDialog(this,"Kết nối server thành công!");
        }catch(IOException e){
            JOptionPane.showMessageDialog(this,"Không thể kết nối server: "+e.getMessage());
        }
    }

    private void sendEmail(){
        if(socket==null || socket.isClosed()){
            JOptionPane.showMessageDialog(this,"Chưa kết nối server!");
            return;
        }
        try{
            String msg = "SEND|"+txtFrom.getText()+"|"+txtTo.getText()+"|"+txtSubject.getText()+"|"+txtContent.getText();
            out.println(msg);
            txtLog.append("Bạn: Gửi email\n");
            txtLog.append("Server: " + in.readLine() + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength()); // auto scroll
        }catch(IOException e){
            JOptionPane.showMessageDialog(this,"Lỗi gửi email: "+e.getMessage());
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new ClientGuiSenderTCP_Fancy().setVisible(true));
    }
}
