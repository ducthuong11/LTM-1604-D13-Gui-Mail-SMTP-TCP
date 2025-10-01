package LTM;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * MailApp - Phiên bản đầy đủ: Inbox count, Drafts, Trash, Save Draft, Restore, ESC to close compose, draggable dialog.
 */
public class ClientGUI extends JFrame {
    // ======== Data class ========
    static class MailMessage implements Serializable, Comparable<MailMessage> {
        String from, to, subject, body;
        Date time;
        boolean isRead = false;
        List<String> attachments = new ArrayList<>(); // lưu đường dẫn file đính kèm


        public MailMessage(String from, String to, String subject, String body) {
            this.from = from; this.to = to; this.subject = subject; this.body = body;
            this.time = new Date(); this.isRead = false;
        }

        public String getTimeString() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(time);
        }

        @Override
        public int compareTo(MailMessage o) {
            return o.time.compareTo(this.time);
        }

        public String preview(int len) {
            if (body == null) return "";
            String s = body.replaceAll("\\s+", " ").trim();
            return s.length() <= len ? s : s.substring(0, len) + "...";
        }
    }

    // ======== Storage ========
    private static final String DATA_FILE = "messages.dat";
    private static Map<String, List<MailMessage>> inboxMap = new HashMap<>();
    private static Map<String, List<MailMessage>> sentMap = new HashMap<>();
    private static Map<String, List<MailMessage>> draftsMap = new HashMap<>();
    private static Map<String, List<MailMessage>> trashMap = new HashMap<>();

    // ======== UI Components ========
    private final String userEmail;
    private final JLabel lblUser = new JLabel();
    private final JTable mailTable;
    private final MailTableModel mailTableModel;
    private final JTextPane mailViewer = new JTextPane();
    private final JTextField searchField = new JTextField(20);
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JButton btnCompose = new JButton("Soạn thư");
    private final JButton btnSentView = new JButton("Đã gửi");
    private final JButton btnRestore = new JButton("Khôi phục");
    private final JButton btnPurge = new JButton("Xóa vĩnh viễn");
    private boolean viewingSent = false;
    private boolean viewingDrafts = false;
    private boolean viewingTrash = false;
    private static boolean viewingSentStatic = false;
    private static boolean viewingDraftsStatic = false;
    private static boolean viewingTrashStatic = false;

    private TableRowSorter<MailTableModel> sorter;
	private boolean darkMode;

    // ======== Mail Table Model ========
    static class MailTableModel extends AbstractTableModel {
        private final String[] cols = {"", "Từ / Đến", "Tiêu đề", "Trích dẫn", "Thời gian"};
        private final List<MailMessage> data = new ArrayList<>();
        private final List<Boolean> checked = new ArrayList<>();
        private final JEditorPane mailViewer = new JEditorPane("text/html",""); 
        private boolean darkMode = false;

        public void setData(List<MailMessage> list) {
            data.clear();
            checked.clear();
            if (list != null) {
                list.sort((a, b) -> b.time.compareTo(a.time));
                data.addAll(list);
                for (int i = 0; i < data.size(); i++) checked.add(false);
            }
            fireTableDataChanged();
        }

        public List<Integer> getCheckedIndices() {
            List<Integer> idx = new ArrayList<>();
            for (int i = 0; i < checked.size(); i++) if (checked.get(i)) idx.add(i);
            return idx;
        }

        public MailMessage getMessageAt(int row) {
            return data.get(row);
        }

        public void removeAt(int row) {
            data.remove(row);
            checked.remove(row);
            fireTableRowsDeleted(row, row);
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }
        @Override public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }

        @Override
        public Object getValueAt(int row, int col) {
            MailMessage m = data.get(row);
            switch (col) {
                case 0: return checked.get(row);
                case 1:
                    if (viewingSentStatic || viewingDraftsStatic) return "Đến: " + m.to;
                    if (viewingTrashStatic) return "Từ: " + m.from + " → " + m.to;
                    return "Từ: " + m.from;
                case 2: return m.subject;
                case 3: return m.preview(80);
                case 4: return new SimpleDateFormat("dd/MM/yy HH:mm").format(m.time);
                default: return "";
            }
        }

        @Override public boolean isCellEditable(int row, int col) { return col == 0; }

        @Override
        public void setValueAt(Object val, int row, int col) {
            if (col == 0) { checked.set(row, Boolean.TRUE.equals(val)); fireTableCellUpdated(row, col); }
        }
    }

    // ======== Constructor ========
    public ClientGUI(String userEmail){
        super("MailApp - "+userEmail);
        this.userEmail = userEmail;

        loadMessages();

        // ===== Header =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(58,123,213));
        lblUser.setText("Xin chào: "+userEmail);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblUser.setBorder(new EmptyBorder(6,8,6,8));
        topBar.add(lblUser, BorderLayout.WEST);

        // Toolbar
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
        toolPanel.setOpaque(false);
        btnCompose.setToolTipText("Soạn thư mới (Ctrl+N)");
        btnCompose.setBackground(new Color(58,123,213)); btnCompose.setForeground(Color.WHITE); btnCompose.setFocusPainted(false);
        btnSentView.setBackground(new Color(58,123,213)); btnSentView.setForeground(Color.WHITE); btnSentView.setFocusPainted(false);
        btnRefresh.setBackground(new Color(58,123,213)); btnRefresh.setForeground(Color.WHITE); btnRefresh.setFocusPainted(false);
        btnDelete.setBackground(new Color(220,53,69)); btnDelete.setForeground(Color.WHITE); btnDelete.setFocusPainted(false);
        btnRestore.setBackground(new Color(40,167,69)); btnRestore.setForeground(Color.WHITE); btnRestore.setFocusPainted(false);
        btnPurge.setBackground(new Color(180,30,30)); btnPurge.setForeground(Color.WHITE); btnPurge.setFocusPainted(false);
        
//        JToggleButton themeBtn = new JToggleButton("🌙 Dark Mode");
//        themeBtn.addActionListener(e -> toggleTheme());
//        toolbar.add(themeBtn);

        toolPanel.add(btnCompose); toolPanel.add(btnSentView); toolPanel.add(btnRefresh); toolPanel.add(btnDelete);
        toolPanel.add(btnRestore); toolPanel.add(btnPurge);
        toolPanel.add(new JLabel("Tìm:")); searchField.setPreferredSize(new Dimension(180,25));
        toolPanel.add(searchField);
        topBar.add(toolPanel, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);
        
        // ===== Sidebar gradient + icon =====
        JPanel left = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,new Color(58,123,213),0,getHeight(),new Color(58,213,123)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(10,10,10,10));
        JButton inboxBtn = new JButton("📥 Hộp thư đến");
        inboxBtn.setIcon(loadIconSafely("icons/inbox.png"));
        inboxBtn.setForeground(Color.WHITE); inboxBtn.setContentAreaFilled(false); inboxBtn.setFocusPainted(false);
        inboxBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        inboxBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));

        JButton draftsBtn = new JButton("📝 Nháp");
        draftsBtn.setIcon(loadIconSafely("icons/draft.png"));
        draftsBtn.setForeground(Color.WHITE); draftsBtn.setContentAreaFilled(false); draftsBtn.setFocusPainted(false);
        draftsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        draftsBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));

        JButton trashBtn = new JButton("🗑️ Thùng rác");
        trashBtn.setIcon(loadIconSafely("icons/trash.png"));
        trashBtn.setForeground(Color.WHITE); trashBtn.setContentAreaFilled(false); trashBtn.setFocusPainted(false);
        trashBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        trashBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));

        left.add(inboxBtn); left.add(Box.createVerticalStrut(8));
        left.add(draftsBtn); left.add(Box.createVerticalStrut(6));
        left.add(trashBtn); left.add(Box.createVerticalGlue());

        // ===== Mail table =====
        mailTableModel = new MailTableModel();
        mailTable = new JTable(mailTableModel);
        mailTable.setRowHeight(28);
        mailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mailTable.getColumnModel().getColumn(0).setMaxWidth(40);
        mailTable.getColumnModel().getColumn(4).setMaxWidth(120);
        mailTable.setAutoCreateRowSorter(true);

        sorter = new TableRowSorter<>(mailTableModel);
        mailTable.setRowSorter(sorter);

        // Renderer stripe + bold + hover
        mailTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            Color even = new Color(245,245,245);
            Color hover = new Color(200,230,255);
            Font plain = null, bold = null;
            int hoveredRow = -1;
            {
                mailTable.addMouseMotionListener(new MouseMotionAdapter(){
                    @Override public void mouseMoved(MouseEvent e){
                        int row = mailTable.rowAtPoint(e.getPoint());
                        if(row!=hoveredRow){ hoveredRow=row; repaint(); }
                    }
                });
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int col){
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
                if(plain==null){ plain=c.getFont().deriveFont(Font.PLAIN); bold=c.getFont().deriveFont(Font.BOLD); }
                int modelRow = table.convertRowIndexToModel(row);
                MailMessage m = mailTableModel.getMessageAt(modelRow);
                c.setFont(m.isRead?plain:bold);
                if(isSelected) c.setBackground(new Color(255,235,205));
                else if(row==hoveredRow) c.setBackground(hover);
                else c.setBackground(row%2==0?even:Color.WHITE);
                return c;
            }
        });

        // ===== Mail viewer =====
        mailViewer.setContentType("text/plain");
        mailViewer.setEditable(false);
        mailViewer.setBackground(new Color(245,250,255));
        mailViewer.setBorder(BorderFactory.createLineBorder(new Color(58,123,213),2));
        mailViewer.setFont(new Font("Segoe UI", Font.PLAIN,14));
     // cho phép click vào link mở file đính kèm
        mailViewer.setContentType("text/html");
        mailViewer.addHyperlinkListener(e -> {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
                try { 
                    Desktop.getDesktop().open(new File(e.getURL().toURI())); 
                }
                catch(Exception ex){ 
                    JOptionPane.showMessageDialog(this,"Không mở được file!"); 
                }
            }
        });


        mailTable.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()){
                int sel = mailTable.getSelectedRow();
                if(sel>=0){
                    int modelIdx = mailTable.convertRowIndexToModel(sel);
                    MailMessage m = mailTableModel.getMessageAt(modelIdx);
                    showMessage(m);
                    if(!m.isRead){ m.isRead=true; saveMessages(); refreshInboxSideAndTable(); }
                } else mailViewer.setText("");
            }
        });

        JSplitPane centerRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(mailTable),new JScrollPane(mailViewer));
        centerRight.setResizeWeight(0.5);
        JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,centerRight);
        outer.setDividerLocation(180);
        add(outer, BorderLayout.CENTER);

        // ===== Button actions =====
        inboxBtn.addActionListener(e->{ viewingSent=false; viewingDrafts=false; viewingTrash=false; viewingSentStatic=false; viewingDraftsStatic=false; viewingTrashStatic=false; refreshInbox(); });
        btnSentView.addActionListener(e->{ viewingSent=true; viewingDrafts=false; viewingTrash=false; viewingSentStatic=true; viewingDraftsStatic=false; viewingTrashStatic=false; refreshSent(); });
        draftsBtn.addActionListener(e->{ viewingDrafts=true; viewingSent=false; viewingTrash=false; viewingDraftsStatic=true; viewingSentStatic=false; viewingTrashStatic=false; refreshDrafts(); });
        trashBtn.addActionListener(e->{ viewingTrash=true; viewingDrafts=false; viewingSent=false; viewingTrashStatic=true; viewingDraftsStatic=false; viewingSentStatic=false; refreshTrash(); });

        btnCompose.addActionListener(e->openComposeDialog());
        btnRefresh.addActionListener(e->doRefresh());
        btnDelete.addActionListener(e->doDeleteSelected());
        btnRestore.addActionListener(e->doRestoreSelected());
        btnPurge.addActionListener(e->doPurgeSelected());

        searchField.getDocument().addDocumentListener(new DocumentListener(){
            public void changedUpdate(DocumentEvent e){ applyFilter(); }
            public void removeUpdate(DocumentEvent e){ applyFilter(); }
            public void insertUpdate(DocumentEvent e){ applyFilter(); }
        });

        viewingSent=false; viewingDrafts=false; viewingTrash=false; viewingSentStatic=false; viewingDraftsStatic=false; viewingTrashStatic=false;
        refreshInbox();

        new javax.swing.Timer(5000,e->{ loadMessages(); if(viewingSent) refreshSent(); else if(viewingDrafts) refreshDrafts(); else if(viewingTrash) refreshTrash(); else refreshInbox(); }).start();
        setupShortcuts();

        setSize(1100,650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private Object toggleTheme() {
		// TODO Auto-generated method stub
		return null;
	}

	// ===== Icon loader safe =====
    private ImageIcon loadIconSafely(String path){
        try{
            java.net.URL r = getClass().getResource("/"+path);
            if(r!=null) return new ImageIcon(r);
            File f = new File(path);
            if(f.exists()) return new ImageIcon(path);
        } catch(Exception ignored){}
        return null;
    }

    // ===== Shortcuts =====
    private void setupShortcuts(){
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks,"compose");
        getRootPane().getActionMap().put("compose", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){ openComposeDialog(); } });

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
        // close window with ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc,"closeWindow");
        getRootPane().getActionMap().put("closeWindow", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e){ dispatchEvent(new WindowEvent(thisClient(), WindowEvent.WINDOW_CLOSING)); } });
    }

    private JFrame thisClient(){ return this; }

    // ===== Compose =====
   private void openComposeDialog() {
    JDialog dlg = new JDialog(this, "Soạn thư", Dialog.ModalityType.APPLICATION_MODAL);
    dlg.setUndecorated(true);
    dlg.setLayout(new BorderLayout());

    // ===== Main panel gradient + rounded =====
    JPanel mainPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0,0,new Color(58,123,213),0,getHeight(),new Color(123,213,58));
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
        }
    };
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    mainPanel.setLayout(new BorderLayout(15,15));

    // Make dialog draggable
    final Point[] mousePt = {null};
    mainPanel.addMouseListener(new MouseAdapter(){
        public void mousePressed(MouseEvent e){ mousePt[0]=e.getPoint(); }
        public void mouseReleased(MouseEvent e){ mousePt[0]=null; }
    });
    mainPanel.addMouseMotionListener(new MouseMotionAdapter(){
        public void mouseDragged(MouseEvent e){
            if(mousePt[0]!=null){
                Point p = e.getLocationOnScreen();
                dlg.setLocation(p.x - mousePt[0].x, p.y - mousePt[0].y);
            }
        }
    });

    // ===== Fields =====
    JPanel fields = new JPanel(new GridBagLayout());
    fields.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5,5,5,5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
    JLabel lblTo = new JLabel("Đến:"); lblTo.setForeground(Color.WHITE); lblTo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    fields.add(lblTo, gbc);
    gbc.gridx = 1; gbc.weightx = 1;
    JTextField toF = new JTextField(); toF.setPreferredSize(new Dimension(400,30));
    toF.setFont(new Font("Segoe UI", Font.PLAIN,14));
    toF.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    fields.add(toF, gbc);

    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
    JLabel lblSubj = new JLabel("Chủ đề:"); lblSubj.setForeground(Color.WHITE); lblSubj.setFont(new Font("Segoe UI", Font.BOLD, 14));
    fields.add(lblSubj, gbc);
    gbc.gridx = 1; gbc.weightx = 1;
    JTextField subjectF = new JTextField(); subjectF.setPreferredSize(new Dimension(400,30));
    subjectF.setFont(new Font("Segoe UI", Font.PLAIN,14));
    subjectF.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    fields.add(subjectF, gbc);

    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
    JTextPane bodyA = new JTextPane(); bodyA.setFont(new Font("Segoe UI",Font.PLAIN,14));
    bodyA.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    bodyA.setBackground(new Color(245,250,255));
    fields.add(new JScrollPane(bodyA), gbc);

    mainPanel.add(fields, BorderLayout.CENTER);

    // ===== Attachments =====
    List<String> tempAttachments = new ArrayList<>();
    DefaultListModel<String> attachModel = new DefaultListModel<>();
    JList<String> attachList = new JList<>(attachModel);
    JScrollPane attachScroll = new JScrollPane(attachList);
    attachScroll.setPreferredSize(new Dimension(200,100));

    // ===== Buttons =====
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 
    buttons.setOpaque(false);

    JButton attach = new JButton("📎 Đính kèm");
    JButton send = new JButton("📧 Gửi"); 
    JButton cancel = new JButton("❌ Hủy");

    styleGradientButton(attach,new Color(100,100,200), new Color(150,150,250));
    styleGradientButton(send,new Color(58,123,213), new Color(58,213,123));
    styleGradientButton(cancel,new Color(220,53,69), new Color(255,85,100));

    attach.setPreferredSize(new Dimension(120,35)); 
    send.setPreferredSize(new Dimension(100,35)); 
    cancel.setPreferredSize(new Dimension(100,35));

    buttons.add(attach); 
    buttons.add(cancel); 
    buttons.add(send);
    mainPanel.add(buttons, BorderLayout.SOUTH);

    // thêm danh sách file bên dưới body
    mainPanel.add(attachScroll, BorderLayout.EAST);

    dlg.add(mainPanel);

    // ESC close
    KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
    dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc,"escape");
    dlg.getRootPane().getActionMap().put("escape", new AbstractAction(){ 
        @Override public void actionPerformed(ActionEvent e){ dlg.dispose(); } 
    });

    // Enter in subject sends
    subjectF.addActionListener(ev->send.doClick());

    // ===== Actions =====
    attach.addActionListener(e->{
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int res = chooser.showOpenDialog(dlg);
        if(res == JFileChooser.APPROVE_OPTION){
            File[] files = chooser.getSelectedFiles();
            for(File f : files){
                tempAttachments.add(f.getAbsolutePath());
                attachModel.addElement(f.getName());
            }
        }
    });

    send.addActionListener(e->{
        String to = toF.getText().trim(), subj = subjectF.getText().trim(), body = bodyA.getText().trim();
        if(to.isEmpty()||subj.isEmpty()){ 
            JOptionPane.showMessageDialog(dlg,"Vui lòng điền người nhận và chủ đề."); 
            return; 
        }
        MailMessage msg = new MailMessage(userEmail,to,subj,body);
        msg.attachments.addAll(tempAttachments); // thêm file đính kèm
        sentMap.computeIfAbsent(userEmail,k->new ArrayList<>()).add(msg);
        inboxMap.computeIfAbsent(to,k->new ArrayList<>()).add(msg);
        saveMessages();
        if(viewingSent) refreshSent(); else if(!viewingSent) refreshInbox();
        JOptionPane.showMessageDialog(dlg,"Đã gửi thư tới "+to);
        dlg.dispose();
    });

    cancel.addActionListener(e->{
        if(!toF.getText().trim().isEmpty() || !subjectF.getText().trim().isEmpty() || !bodyA.getText().trim().isEmpty()){
            int ans = JOptionPane.showConfirmDialog(dlg,"Lưu nháp trước khi đóng?","Lưu nháp",JOptionPane.YES_NO_CANCEL_OPTION);
            if(ans==JOptionPane.CANCEL_OPTION) return;
            if(ans==JOptionPane.YES_OPTION){
                MailMessage draft = new MailMessage(userEmail,toF.getText().trim(),subjectF.getText().trim(),bodyA.getText().trim());
                draft.attachments.addAll(tempAttachments);
                draftsMap.computeIfAbsent(userEmail,k->new ArrayList<>()).add(draft);
                saveMessages();
                if(viewingDrafts) refreshDrafts();
            }
        }
        dlg.dispose();
    });

    dlg.setSize(700,500);
    dlg.setLocationRelativeTo(this);
    dlg.setVisible(true);
}


    // ===== Hàm tạo button gradient + hover =====
    private void styleGradientButton(JButton btn, Color start, Color end) {
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setCursor(Cursor.getDefaultCursor()); }
        });
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,start,0,c.getHeight(),end));
                g2.fillRoundRect(0,0,c.getWidth(),c.getHeight(),15,15);
                super.paint(g, c);
            }
        });
    }

    // ===== Show message =====
   private void showMessage(MailMessage m){
    StringBuilder sb = new StringBuilder();
    sb.append("<html><body style='font-family:Segoe UI; font-size:13px;'>");
    sb.append("<b>Từ:</b> ").append(m.from).append("<br>");
    sb.append("<b>Đến:</b> ").append(m.to).append("<br>");
    sb.append("<b>Chủ đề:</b> ").append(m.subject).append("<br>");
    sb.append("<b>Thời gian:</b> ").append(m.getTimeString()).append("<br><br>");
    sb.append("<div style='white-space:pre-wrap;'>").append(m.body).append("</div><br><br>");

    if(!m.attachments.isEmpty()){
        sb.append("<b>Đính kèm:</b><br>");
        for(String path : m.attachments){
            File f = new File(path);
            String name = f.getName();
            if(name.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif)$")){
                // hiển thị ảnh inline
                sb.append("<img src='file:").append(f.getAbsolutePath()).append("' style='max-width:40px;'><br>");
            } else {
                // hiển thị link để mở file
                sb.append("<a href='file:").append(f.getAbsolutePath()).append("'>📎 ").append(name).append("</a><br>");
            }
        }
    }
// private void toggleTheme() {
//        darkMode = !darkMode;
//        if (darkMode) {
//            UIManager.put("control", new Color(45,45,45));
//            UIManager.put("info", new Color(60,60,60));
//            UIManager.put("nimbusBase", new Color(18,30,49));
//            UIManager.put("nimbusAlertYellow", new Color(248,187,0));
//            UIManager.put("nimbusDisabledText", new Color(128,128,128));
//            UIManager.put("nimbusFocus", new Color(115,164,209));
//            UIManager.put("nimbusGreen", new Color(176,179,50));
//            UIManager.put("nimbusInfoBlue", new Color(66,139,221));
//            UIManager.put("nimbusLightBackground", new Color(45,45,45));
//            UIManager.put("nimbusOrange", new Color(191,98,4));
//            UIManager.put("nimbusRed", new Color(169,46,34));
//            UIManager.put("nimbusSelectedText", new Color(255,255,255));
//            UIManager.put("nimbusSelectionBackground", new Color(104,93,156));
//            UIManager.put("text", new Color(230,230,230));
//        } else {
//            UIManager.put("control", null);
//            UIManager.put("info", null);
//            UIManager.put("nimbusBase", null);
//            UIManager.put("nimbusAlertYellow", null);
//            UIManager.put("nimbusDisabledText", null);
//            UIManager.put("nimbusFocus", null);
//            UIManager.put("nimbusGreen", null);
//            UIManager.put("nimbusInfoBlue", null);
//            UIManager.put("nimbusLightBackground", null);
//            UIManager.put("nimbusOrange", null);
//            UIManager.put("nimbusRed", null);
//            UIManager.put("nimbusSelectedText", null);
//            UIManager.put("nimbusSelectionBackground", null);
//            UIManager.put("text", null);
//        }
//
//        SwingUtilities.updateComponentTreeUI(this); // refresh toàn bộ UI
//        this.pack();
//    }


    sb.append("</body></html>");
    mailViewer.setText(sb.toString());
    mailViewer.setCaretPosition(0);
}


    // ===== Delete =====
    private void doDeleteSelected(){
        List<Integer> checks = mailTableModel.getCheckedIndices();
        if(checks.isEmpty()){ JOptionPane.showMessageDialog(this,"Vui lòng chọn thư cần xóa."); return; }

        if(viewingTrash){
            // Permanently delete selected from trash
            int confirm = JOptionPane.showConfirmDialog(this,"Xóa vĩnh viễn "+checks.size()+" thư khỏi Thùng rác?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(confirm!=JOptionPane.YES_OPTION) return;
            Collections.sort(checks,Collections.reverseOrder());
            for(int idx:checks){
                MailMessage m = mailTableModel.getMessageAt(idx);
                trashMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                mailTableModel.removeAt(idx);
            }
            saveMessages();
            mailViewer.setText("");
            refreshTrash();
        } else if(viewingDrafts){
            int confirm = JOptionPane.showConfirmDialog(this,"Xóa "+checks.size()+" nháp?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(confirm!=JOptionPane.YES_OPTION) return;
            Collections.sort(checks,Collections.reverseOrder());
            for(int idx:checks){
                MailMessage m = mailTableModel.getMessageAt(idx);
                draftsMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                mailTableModel.removeAt(idx);
            }
            saveMessages();
            mailViewer.setText("");
            refreshDrafts();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,"Chuyển "+checks.size()+" thư vào Thùng rác?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(confirm!=JOptionPane.YES_OPTION) return;
            Collections.sort(checks,Collections.reverseOrder());
            for(int idx:checks){
                MailMessage m = mailTableModel.getMessageAt(idx);
                // determine source and remove from source
                inboxMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                sentMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                trashMap.computeIfAbsent(userEmail,k->new ArrayList<>()).add(m);
                mailTableModel.removeAt(idx);
            }
            saveMessages();
            mailViewer.setText("");
            refreshInboxSideAndTable();
        }
    }

    // ===== Restore from trash =====
    private void doRestoreSelected(){
        if(!viewingTrash){ JOptionPane.showMessageDialog(this,"Chỉ có thể khôi phục khi xem Thùng rác."); return; }
        List<Integer> checks = mailTableModel.getCheckedIndices();
        if(checks.isEmpty()){ JOptionPane.showMessageDialog(this,"Vui lòng chọn thư cần khôi phục."); return; }
        Collections.sort(checks, Collections.reverseOrder());
        for(int idx:checks){
            MailMessage m = mailTableModel.getMessageAt(idx);
            // if current user is recipient -> restore to inbox, else if sender -> restore to sent
            if(userEmail.equalsIgnoreCase(m.to)){
                inboxMap.computeIfAbsent(userEmail,k->new ArrayList<>()).add(m);
            } else if(userEmail.equalsIgnoreCase(m.from)){
                sentMap.computeIfAbsent(userEmail,k->new ArrayList<>()).add(m);
            } else {
                // if ambiguous, put to inbox by default
                inboxMap.computeIfAbsent(userEmail,k->new ArrayList<>()).add(m);
            }
            trashMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
            mailTableModel.removeAt(idx);
        }
        saveMessages();
        refreshTrash();
        refreshInboxSideAndTable();
    }

    // ===== Purge (alias permanent delete) for any view if wanted =====
    private void doPurgeSelected(){
        List<Integer> checks = mailTableModel.getCheckedIndices();
        if(checks.isEmpty()){ JOptionPane.showMessageDialog(this,"Vui lòng chọn thư."); return; }
        if(viewingTrash){
            int confirm = JOptionPane.showConfirmDialog(this,"Xóa vĩnh viễn "+checks.size()+" thư?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(confirm!=JOptionPane.YES_OPTION) return;
            Collections.sort(checks,Collections.reverseOrder());
            for(int idx:checks){
                MailMessage m = mailTableModel.getMessageAt(idx);
                trashMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                mailTableModel.removeAt(idx);
            }
            saveMessages();
            refreshTrash();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,"Xóa vĩnh viễn "+checks.size()+" thư từ nguồn hiện tại?","Xác nhận",JOptionPane.YES_NO_OPTION);
            if(confirm!=JOptionPane.YES_OPTION) return;
            Collections.sort(checks,Collections.reverseOrder());
            for(int idx:checks){
                MailMessage m = mailTableModel.getMessageAt(idx);
                inboxMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                sentMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                draftsMap.getOrDefault(userEmail,new ArrayList<>()).remove(m);
                mailTableModel.removeAt(idx);
            }
            saveMessages();
            refreshInboxSideAndTable();
        }
    }

    // ===== Refresh =====
    private void doRefresh(){ loadMessages(); if(viewingSent) refreshSent(); else if(viewingDrafts) refreshDrafts(); else if(viewingTrash) refreshTrash(); else refreshInbox(); }
    private void refreshInboxSideAndTable(){ refreshInbox(); if(viewingSent) refreshSent(); if(viewingDrafts) refreshDrafts(); if(viewingTrash) refreshTrash(); }
    private void refreshInbox(){
        viewingSent=false; viewingDrafts=false; viewingTrash=false;
        viewingSentStatic=false; viewingDraftsStatic=false; viewingTrashStatic=false;
        mailTableModel.setData(inboxMap.getOrDefault(userEmail,new ArrayList<>()));
        updateSideLabels();
    }
    private void refreshSent(){
        viewingSent=true; viewingDrafts=false; viewingTrash=false;
        viewingSentStatic=true; viewingDraftsStatic=false; viewingTrashStatic=false;
        mailTableModel.setData(sentMap.getOrDefault(userEmail,new ArrayList<>()));
        updateSideLabels();
    }
    private void refreshDrafts(){
        viewingDrafts=true; viewingSent=false; viewingTrash=false;
        viewingDraftsStatic=true; viewingSentStatic=false; viewingTrashStatic=false;
        mailTableModel.setData(draftsMap.getOrDefault(userEmail,new ArrayList<>()));
        updateSideLabels();
    }
    private void refreshTrash(){
        viewingTrash=true; viewingSent=false; viewingDrafts=false;
        viewingTrashStatic=true; viewingSentStatic=false; viewingDraftsStatic=false;
        mailTableModel.setData(trashMap.getOrDefault(userEmail,new ArrayList<>()));
        updateSideLabels();
    }

    private void updateSideLabels(){
        List<MailMessage> inbox = inboxMap.getOrDefault(userEmail,new ArrayList<>());
        long unread = inbox.stream().filter(m->!m.isRead).count();
        long total = inbox.size();

        // Update window title or a dedicated label could be used; for simplicity we'll update lblUser tooltip
        lblUser.setToolTipText("Hộp thư đến: " + total + " (Chưa đọc: " + unread + ")");

        // Optionally update button texts (to show counts)
        // find the left panel buttons by searching components (not ideal, but we will update via traversal)
        // For clearer API, we will just update title bar and show a small floating label on left - but here we update existing left buttons by scanning.
        // Instead of scanning, we'll set the frame's title to include counts as well:
        setTitle(String.format("MailApp - %s   • Hộp thư: %d (Chưa đọc: %d)  • Nháp: %d  • Thùng rác: %d",
                userEmail,
                total,
                unread,
                draftsMap.getOrDefault(userEmail,new ArrayList<>()).size(),
                trashMap.getOrDefault(userEmail,new ArrayList<>()).size()
        ));
    }

    // ===== Search using RowFilter =====
    private void applyFilter(){
        String q = searchField.getText().trim().toLowerCase();
        if(q.isEmpty()){ sorter.setRowFilter(null); return; }
        final String qq = q;
        sorter.setRowFilter(new RowFilter<MailTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends MailTableModel, ? extends Integer> entry) {
                MailTableModel m = entry.getModel();
                int idx = entry.getIdentifier();
                if(idx < 0 || idx >= m.getRowCount()) return false;
                MailMessage mm = m.getMessageAt(idx);
                return (mm.subject!=null && mm.subject.toLowerCase().contains(qq))
                        || (mm.from!=null && mm.from.toLowerCase().contains(qq))
                        || (mm.to!=null && mm.to.toLowerCase().contains(qq))
                        || (mm.body!=null && mm.body.toLowerCase().contains(qq));
            }
        });
    }

    // ===== Persistence =====
    @SuppressWarnings("unchecked")
    private void loadMessages(){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))){
            inboxMap = (Map<String,List<MailMessage>>)ois.readObject();
            sentMap = (Map<String,List<MailMessage>>)ois.readObject();
            draftsMap = (Map<String,List<MailMessage>>)ois.readObject();
            trashMap = (Map<String,List<MailMessage>>)ois.readObject();
        } catch(Exception e){
            inboxMap=new HashMap<>(); sentMap=new HashMap<>(); draftsMap=new HashMap<>(); trashMap=new HashMap<>();
        }
    }
    private void saveMessages(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))){
            oos.writeObject(inboxMap); oos.writeObject(sentMap); oos.writeObject(draftsMap); oos.writeObject(trashMap);
        } catch(Exception e){ e.printStackTrace(); }
    }

    // ===== Main =====
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            String email = JOptionPane.showInputDialog(null,"Nhập email để mở mailbox:","Đăng nhập tạm",JOptionPane.PLAIN_MESSAGE);
            if(email==null||email.trim().isEmpty()) return;
            if(!email.endsWith("@gmail.com")) { JOptionPane.showMessageDialog(null,"Vui lòng nhập email dạng @gmail.com"); return; }
            new ClientGUI(email.trim());
        });
    }
}
