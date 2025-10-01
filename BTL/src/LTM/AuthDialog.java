package LTM;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AuthDialog extends JDialog {
    private boolean isLogin = true;
    private JTextField emailField;
    private JPasswordField passField, confirmField;
    private JButton actionBtn, switchBtn;

    private static Map<String,String> users = new HashMap<>();
    private static final File usersFile = new File("users.ser"); // file lưu tài khoản

    private boolean succeeded = false;
    private String userEmail;

    public AuthDialog(JFrame parent){
        super(parent,true);
        setUndecorated(true);
        setSize(420,360);
        setLocationRelativeTo(parent);

        // nạp tài khoản từ file
        loadUsers();

        JPanel mainPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,new Color(58,123,213),0,getHeight(),new Color(123,213,58));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        add(mainPanel);

        JLabel titleLabel = new JLabel("Đăng nhập", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI",Font.BOLD,22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0,20,420,30);
        mainPanel.add(titleLabel);

        emailField = new JTextField();
        emailField.setBounds(60, 70, 300, 35);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addFocusAnimation(emailField);
        mainPanel.add(emailField);
        addPlaceholder(emailField,"Nhập email của bạn...");

        JLabel emailLbl = new JLabel("Email"); emailLbl.setForeground(Color.WHITE);
        emailLbl.setBounds(60,50,100,20); mainPanel.add(emailLbl);

        passField = new JPasswordField();
        passField.setBounds(60,130,300,35);
        passField.setFont(new Font("Segoe UI",Font.PLAIN,14));
        addFocusAnimation(passField);
        mainPanel.add(passField);
        addPlaceholder(passField,"Nhập mật khẩu...");

        JLabel passLbl = new JLabel("Mật khẩu"); passLbl.setForeground(Color.WHITE);
        passLbl.setBounds(60,110,100,20); mainPanel.add(passLbl);

        confirmField = new JPasswordField();
        confirmField.setBounds(60,190,300,35);
        confirmField.setFont(new Font("Segoe UI",Font.PLAIN,14));
        addFocusAnimation(confirmField);
        mainPanel.add(confirmField);
        addPlaceholder(confirmField,"Xác nhận mật khẩu...");
        confirmField.setVisible(false);

        JLabel confirmLbl = new JLabel("Xác nhận"); confirmLbl.setForeground(Color.WHITE);
        confirmLbl.setBounds(60,170,120,20); mainPanel.add(confirmLbl);
        confirmLbl.setVisible(false);

        actionBtn = new JButton("Đăng nhập"); 
        styleGradientButton(actionBtn,new Color(58,123,213),new Color(58,213,123));
        actionBtn.setBounds(60,250,140,40); mainPanel.add(actionBtn);

        switchBtn = new JButton("Đăng ký mới"); 
        styleGradientButton(switchBtn,new Color(220,53,69),new Color(255,85,100));
        switchBtn.setBounds(220,250,140,40); mainPanel.add(switchBtn);

        // ===== Actions =====
        actionBtn.addActionListener(e->doAction(titleLabel,confirmLbl,confirmField));
        switchBtn.addActionListener(e->toggleMode(titleLabel,confirmLbl,confirmField));

        setVisible(true);
    }

    private void doAction(JLabel title,JLabel confirmLbl,JPasswordField confirmFld){
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        if(email.isEmpty()||pass.isEmpty()){
            JOptionPane.showMessageDialog(this,"Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if(!isValidEmail(email)){
            JOptionPane.showMessageDialog(this,"Email không hợp lệ!");
            return;
        }

        if(isLogin){
            if(users.containsKey(email) && users.get(email).equals(pass)){
                succeeded=true; userEmail=email; dispose();
            } else JOptionPane.showMessageDialog(this,"Sai email hoặc mật khẩu!");
        } else {
            String confirmPass = new String(confirmFld.getPassword());
            if(!pass.equals(confirmPass)){
                JOptionPane.showMessageDialog(this,"Mật khẩu xác nhận không khớp!");
                return;
            }
            if(users.containsKey(email)){
                JOptionPane.showMessageDialog(this,"Email đã tồn tại!");
                return;
            }
            users.put(email,pass);
            saveUsers(); // lưu lại sau khi đăng ký
            JOptionPane.showMessageDialog(this,"Đăng ký thành công! Bây giờ đăng nhập.");
            toggleMode(title,confirmLbl,confirmFld);
        }
    }

    private void toggleMode(JLabel title,JLabel confirmLbl,JPasswordField confirmFld){
        isLogin=!isLogin;
        if(isLogin){
            title.setText("Đăng nhập"); actionBtn.setText("Đăng nhập"); switchBtn.setText("Đăng ký mới");
            confirmLbl.setVisible(false); confirmFld.setVisible(false);
        } else {
            title.setText("Đăng ký"); actionBtn.setText("Đăng ký"); switchBtn.setText("Đăng nhập");
            confirmLbl.setVisible(true); confirmFld.setVisible(true);
        }
    }

    private boolean isValidEmail(String email){
        String regex = "^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$";
        return Pattern.matches(regex,email);
    }

    private void addFocusAnimation(JComponent comp){
        comp.addFocusListener(new FocusAdapter(){
            Border normal = BorderFactory.createLineBorder(Color.WHITE,2);
            Border focus = BorderFactory.createLineBorder(Color.YELLOW,2);
            @Override
            public void focusGained(FocusEvent e){ comp.setBorder(focus);}
            @Override
            public void focusLost(FocusEvent e){ comp.setBorder(normal);}
        });
    }

    private void addPlaceholder(JTextComponent comp,String text){
        comp.setText(text); comp.setForeground(Color.LIGHT_GRAY);
        comp.addFocusListener(new FocusAdapter(){
            @Override
            public void focusGained(FocusEvent e){
                if(comp.getText().equals(text)){ comp.setText(""); comp.setForeground(Color.BLACK);}
            }
            @Override
            public void focusLost(FocusEvent e){
                if(comp.getText().isEmpty()){ comp.setText(text); comp.setForeground(Color.LIGHT_GRAY);}
            }
        });
    }

    private void styleGradientButton(JButton btn,Color start,Color end){
        btn.setContentAreaFilled(false); btn.setFocusPainted(false); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,14));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI(){
            @Override
            public void paint(Graphics g,JComponent c){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,start,0,c.getHeight(),end));
                g2.fillRoundRect(0,0,c.getWidth(),c.getHeight(),15,15);
                super.paint(g,c);
            }
        });
        btn.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(MouseEvent e){ btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
            public void mouseExited(MouseEvent e){ btn.setCursor(Cursor.getDefaultCursor());}
        });
    }

    // ====== Lưu / nạp tài khoản ======
    @SuppressWarnings("unchecked")
    private void loadUsers(){
        if(usersFile.exists()){
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(usersFile))){
                users = (Map<String,String>) ois.readObject();
            } catch(Exception e){ e.printStackTrace(); }
        }
    }

    private void saveUsers(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersFile))){
            oos.writeObject(users);
        } catch(Exception e){ e.printStackTrace(); }
    }

    public boolean isSucceeded(){ return succeeded; }
    public String getUserEmail(){ return userEmail; }
}
