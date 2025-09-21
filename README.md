<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>>
<h2 align="center">
   Gửi email mô phỏng SMTP qua Socket
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

## 📖 1. Giới thiệu
Tên đề tài: Gửi email mô phỏng SMTP qua Socket
Mục tiêu:
Hiểu cơ chế hoạt động cơ bản của giao thức SMTP.
Thực hành lập trình Socket trong Java để mô phỏng quá trình gửi/nhận email.
Xây dựng mô hình Client – Server đơn giản:
Client: gửi lệnh SMTP và nội dung email.
Server: phản hồi các mã trạng thái, lưu và hiển thị email.
Ý nghĩa:
Sinh viên nắm được cách thức hoạt động của các giao thức tầng ứng dụng.
Ứng dụng được vào các bài toán lập trình mạng nâng cao (xây dựng mail relay, bảo mật bằng TLS, xác thực tài khoản…).

Thành phần hệ thống
**Khởi động chương trình**
   - Hiển thị menu hướng dẫn nhập lệnh từng bước.

**Gửi lệnh HELO**
   - Người dùng nhập tên → gửi `HELO <tên>` đến Server.
   - Nhận phản hồi `250 Hello <tên>`.

**Gửi lệnh MAIL FROM**
   - Người dùng nhập email người gửi.
   - Gửi `MAIL FROM:<email>`.
   - Nhận phản hồi từ Server.

**Gửi lệnh RCPT TO**
   - Người dùng nhập email người nhận.
   - Gửi `RCPT TO:<email>`.
   - Nhận phản hồi từ Server.

**Gửi lệnh DATA + nội dung email**
   - Client gửi `DATA`.
   - Sau đó cho phép người dùng nhập nhiều dòng nội dung.
   - Kết thúc khi người dùng nhập dấu `.`.

**Gửi lệnh QUIT**
   - Client gửi `QUIT`.
   - Nhận phản hồi `221 Bye`.
   - Hiển thị:  
     ```
     ✅ Phiên SMTP đã kết thúc.
     ```

---

## 🛠️ 2. Công nghệ sử dụng
- Ngôn ngữ lập trình: Java 23 SE
- Thư viện:
java.net.ServerSocket, java.net.Socket (xử lý TCP).
BufferedReader, PrintWriter (gửi/nhận dữ liệu dạng text).
-Giao thức: SMTP (Simple Mail Transfer Protocol).
-Công cụ IDE: Eclipse / IntelliJ IDEA.
-Môi trường chạy: Windows / Linux / macOS.

## 🚀 3. Hình ảnh các chức năng
<img width="531" height="364" alt="image" src="https://github.com/user-attachments/assets/b07d2d2e-7aea-4359-836d-08d9ef018148" />
<img width="532" height="439" alt="image" src="https://github.com/user-attachments/assets/7ce587be-5acb-4abb-bdd4-04ec8f515d07" />
<img width="531" height="365" alt="image" src="https://github.com/user-attachments/assets/5b47b063-d8b4-495a-ac2e-2df888c0bc0a" />
<img width="536" height="432" alt="image" src="https://github.com/user-attachments/assets/07cd801b-6f14-4090-a0d9-4de822c9786b" />





## ⚙️ 4. Các bước cài đặt & Chạy ứng dụng
🛠️ Yêu cầu hệ thống
- Hệ điều hành: Windows 10/11, macOS, Linux.
- Java Development Kit (JDK): Phiên bản 8 trở lên
- RAM: Tối thiểu 2GB
IDE: Eclipse
🚀 Clone source code
Mở terminal/cmd và chạy lệnh:

git clone https://github.com/ducthuong11/LTM-1604-D13-Gui-Mail-SMTP-TCP.git
cd LTM-1604-D13-Gui-Mail-SMTP-TCP

📥 Các bước chạy chương trình

1. Khởi động Server

- Chạy server trước để lắng nghe kết nối TCP:'

  java SMTPServer

- Server đã sẵn sàng nhận kết nối từ client.

2. Khởi động Client gửi email
- Mở terminal/cmd mới, chạy:

java ClientGuiSenderTCP_Fancy

👉 Một cửa sổ GUI hiện ra cho phép nhập:

Người nhận

Tiêu đề

Nội dung

Sau đó nhấn Gửi để gửi email đến server.

3. Khởi động Client nhận email
- Mở thêm terminal/cmd khác, chạy:

java ClientGuiReceiver

👉 Cửa sổ GUI hiển thị các email đã nhận từ server.

✅ Lưu ý:

- Mỗi client phải kết nối server trước khi gửi/nhận email.

- Tên người nhận phải trùng với client đã đăng ký để push notification hoạt động.

- Có thể chạy nhiều client Người Nhận cùng lúc → server push email tới từng client tương ứng.

##📞 5. Liên hệ
Nếu bạn có bất kỳ thắc mắc hay góp ý nào, vui lòng liên hệ:

👤 Họ và tên: Nguyễn Đức Thường
🎓 Lớp: CNTT 16-04
📍 Địa chỉ: Hà Đông, Hà Nội
📧 Email: ducthuong246ss@gmail.com
📱 Số điện thoại: 0865879212
