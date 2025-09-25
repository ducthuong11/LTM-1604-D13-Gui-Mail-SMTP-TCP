package LTM;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

// Lớp lưu trữ tin nhắn
class MailMessage implements Serializable {
    String from;
    String to;
    String subject;
    String body;
    String time;

    public MailMessage(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }

    public String toString() {
        return "📩 [" + time + "] " + subject + " - Từ: " + from + " ➝ Đến: " + to;
    }
}

public class ClientGUI {
    private static final String DATA_FILE = "messages.dat";
    private static Map<String, java.util.List<MailMessage>> inboxMap = new HashMap<>();
    private static Map<String, java.util.List<MailMessage>> sentMap = new HashMap<>();

    // --- Giao diện đăng nhập ---
    static class LoginForm extends JFrame {
        JTextField emailField;
        JButton loginBtn;

        LoginForm() {
            setTitle("Đăng nhập Mail");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("📧 GMail", JLabel.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(title);

            emailField = new JTextField();
            emailField.setBorder(BorderFactory.createTitledBorder("Nhập Gmail"));
            panel.add(emailField);

            loginBtn = new JButton("Đăng nhập");
            loginBtn.setBackground(new Color(66, 133, 244));
            loginBtn.setForeground(Color.WHITE);
            panel.add(loginBtn);

            add(panel);

            emailField.addActionListener(e -> doLogin());
            loginBtn.addActionListener(e -> doLogin());
        }

        void doLogin() {
            String email = emailField.getText().trim();
            if (!email.endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ Gmail hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            new MailClient(email).setVisible(true);
        }
    }

    // --- Giao diện Mail Client ---
    static class MailClient extends JFrame {
        String userEmail;
        DefaultListModel<MailMessage> inboxModel = new DefaultListModel<>();
        DefaultListModel<MailMessage> sentModel = new DefaultListModel<>();
        JList<MailMessage> inboxList = new JList<>(inboxModel);
        JList<MailMessage> sentList = new JList<>(sentModel);

        JTextField toField, subjectField;
        JTextArea bodyArea;
        JTextArea inboxContentArea = new JTextArea();
        JTextArea sentContentArea = new JTextArea();

        MailClient(String email) {
            this.userEmail = email;
            setTitle("MailApp - " + userEmail);
            setSize(700, 500);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            inboxContentArea.setLineWrap(true);
            inboxContentArea.setWrapStyleWord(true);
            inboxContentArea.setEditable(false);

            sentContentArea.setLineWrap(true);
            sentContentArea.setWrapStyleWord(true);
            sentContentArea.setEditable(false);

            JTabbedPane tabs = new JTabbedPane();

            // --- Tab Soạn thư ---
            JPanel composePanel = new JPanel(new BorderLayout(10, 10));

            toField = new JTextField();
            toField.setBorder(BorderFactory.createTitledBorder("Đến"));

            subjectField = new JTextField();
            subjectField.setBorder(BorderFactory.createTitledBorder("Tiêu đề"));

            bodyArea = new JTextArea();
            bodyArea.setLineWrap(true);
            bodyArea.setWrapStyleWord(true);
            JScrollPane bodyScroll = new JScrollPane(bodyArea);
            bodyScroll.setBorder(BorderFactory.createTitledBorder("Nội dung"));

            JPanel fieldsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            fieldsPanel.add(toField);
            fieldsPanel.add(subjectField);

            composePanel.add(fieldsPanel, BorderLayout.NORTH);
            composePanel.add(bodyScroll, BorderLayout.CENTER);

            JButton sendBtn = new JButton("Gửi ✉️");
            sendBtn.setBackground(new Color(52, 168, 83));
            sendBtn.setForeground(Color.WHITE);
            composePanel.add(sendBtn, BorderLayout.SOUTH);

            tabs.addTab("✉️ Soạn thư", composePanel);

            // --- Tab Hộp thư đến ---
            JPanel inboxPanel = new JPanel(new BorderLayout());
            inboxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JSplitPane inboxSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(inboxList), new JScrollPane(inboxContentArea));
            inboxSplit.setDividerLocation(200);
            inboxPanel.add(inboxSplit, BorderLayout.CENTER);

            JButton deleteInboxBtn = new JButton("Xóa 🗑️");
            JButton refreshInboxBtn = new JButton("Làm mới 📥");
            refreshInboxBtn.setBackground(new Color(255, 193, 7));
            refreshInboxBtn.setForeground(Color.BLACK);

            JPanel inboxBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            inboxBtnPanel.add(deleteInboxBtn);
            inboxBtnPanel.add(refreshInboxBtn);
            inboxPanel.add(inboxBtnPanel, BorderLayout.SOUTH);

            tabs.addTab("📥 Hộp thư đến", inboxPanel);

            // --- Tab Đã gửi ---
            JPanel sentPanel = new JPanel(new BorderLayout());
            sentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JSplitPane sentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    new JScrollPane(sentList), new JScrollPane(sentContentArea));
            sentSplit.setDividerLocation(200);
            sentPanel.add(sentSplit, BorderLayout.CENTER);

            JButton deleteSentBtn = new JButton("Xóa 🗑️");
            JButton refreshSentBtn = new JButton("Làm mới 📤");
            refreshSentBtn.setBackground(new Color(255, 193, 7));
            refreshSentBtn.setForeground(Color.BLACK);

            JPanel sentBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            sentBtnPanel.add(deleteSentBtn);
            sentBtnPanel.add(refreshSentBtn);
            sentPanel.add(sentBtnPanel, BorderLayout.SOUTH);

            tabs.addTab("📤 Đã gửi", sentPanel);

            // --- Thanh trên cùng ---
            JButton logoutBtn = new JButton("Đăng xuất");
            logoutBtn.setBackground(new Color(234, 67, 53));
            logoutBtn.setForeground(Color.WHITE);
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.add(new JLabel("Xin chào: " + userEmail, JLabel.LEFT), BorderLayout.WEST);
            topBar.add(logoutBtn, BorderLayout.EAST);

            add(topBar, BorderLayout.NORTH);
            add(tabs, BorderLayout.CENTER);

            // --- Load dữ liệu ---
            refreshInbox();
            refreshSent();

            // --- Gửi mail ---
            sendBtn.addActionListener(e -> sendMail());
            bodyArea.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                        sendMail();
                    }
                }
            });

            // --- Xóa mail ---
            deleteInboxBtn.addActionListener(e -> deleteMessage(inboxList, inboxModel, inboxMap));
            deleteSentBtn.addActionListener(e -> deleteMessage(sentList, sentModel, sentMap));

            // --- Làm mới ---
            refreshInboxBtn.addActionListener(e -> refreshInbox());
            refreshSentBtn.addActionListener(e -> refreshSent());

            // --- Đăng xuất ---
            logoutBtn.addActionListener(e -> {
                saveMessages();
                dispose();
                new LoginForm().setVisible(true);
            });

            // --- Hiển thị nội dung mail ---
            inboxList.addListSelectionListener(e -> {
                MailMessage msg = inboxList.getSelectedValue();
                if (msg != null) {
                    inboxContentArea.setText(
                            "Từ: " + msg.from + "\n" +
                                    "Đến: " + msg.to + "\n" +
                                    "Tiêu đề: " + msg.subject + "\n" +
                                    "Thời gian: " + msg.time + "\n\n" +
                                    msg.body
                    );
                }
            });

            sentList.addListSelectionListener(e -> {
                MailMessage msg = sentList.getSelectedValue();
                if (msg != null) {
                    sentContentArea.setText(
                            "Từ: " + msg.from + "\n" +
                                    "Đến: " + msg.to + "\n" +
                                    "Tiêu đề: " + msg.subject + "\n" +
                                    "Thời gian: " + msg.time + "\n\n" +
                                    msg.body
                    );
                }
            });

            // --- Tự động làm mới mỗi 5 giây ---
            javax.swing.Timer autoRefresh = new javax.swing.Timer(5000, e -> {
                loadMessages();
                refreshInbox();
                refreshSent();
            });
            autoRefresh.start();
        }

        // --- Các phương thức ---
        void sendMail() {
            String to = toField.getText().trim();
            String subject = subjectField.getText().trim();
            String body = bodyArea.getText().trim();

            if (to.isEmpty() || !to.endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Địa chỉ người nhận không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MailMessage msg = new MailMessage(userEmail, to, subject, body);
            sentMap.computeIfAbsent(userEmail, k -> new ArrayList<>()).add(msg);
            inboxMap.computeIfAbsent(to, k -> new ArrayList<>()).add(msg);

            saveMessages();
            JOptionPane.showMessageDialog(this, "✅ Gửi mail thành công!");
            toField.setText("");
            subjectField.setText("");
            bodyArea.setText("");

            refreshInbox();
            refreshSent();
        }

        void deleteMessage(JList<MailMessage> list, DefaultListModel<MailMessage> model,
                           Map<String, java.util.List<MailMessage>> storage) {
            int index = list.getSelectedIndex();
            if (index >= 0) {
                MailMessage msg = model.get(index);
                model.remove(index);
                storage.getOrDefault(userEmail, new ArrayList<>()).remove(msg);
                saveMessages();
            }
        }

        void refreshInbox() {
            inboxModel.clear();
            java.util.List<MailMessage> list = inboxMap.getOrDefault(userEmail, new ArrayList<>());
            list.sort((a, b) -> b.time.compareTo(a.time));
            list.forEach(inboxModel::addElement);
        }

        void refreshSent() {
            sentModel.clear();
            java.util.List<MailMessage> list = sentMap.getOrDefault(userEmail, new ArrayList<>());
            list.sort((a, b) -> b.time.compareTo(a.time));
            list.forEach(sentModel::addElement);
        }
    }

    // --- Lưu & tải dữ liệu ---
    @SuppressWarnings("unchecked")
    private static void loadMessages() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            inboxMap = (Map<String, java.util.List<MailMessage>>) ois.readObject();
            sentMap = (Map<String, java.util.List<MailMessage>>) ois.readObject();
        } catch (Exception ignored) {}
    }

    private static void saveMessages() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(inboxMap);
            oos.writeObject(sentMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Main ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
