package LTM;

import javax.swing.SwingUtilities;

public class main {  // <- phải có lớp bao bên ngoài
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Tạo AuthDialog (form đăng nhập/đăng ký)
            AuthDialog auth = new AuthDialog(null);

            // 2. Nếu đăng nhập thành công
            if (auth.isSucceeded()) {
                // Mở ClientGUI với email đã đăng nhập
                new ClientGUI(auth.getUserEmail());
            } else {
                // Nếu thoát hoặc đăng nhập thất bại -> đóng app
                System.exit(0);
            }
        });
    }
}
