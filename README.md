<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   NETWORK PROGRAMMING
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

## 🏗️ 2. Thành phần hệ thống
### 🔹 Chức năng của Server (SMTP Server)
1. **Khởi động và lắng nghe**
   - Server mở cổng `9999` để chờ kết nối.
   - In ra thông báo:
     ```
     📡 UDP SMTP Server đang chạy tại cổng 9999
     ```

2. **Xử lý lệnh HELO**
   - Nhận:  
     ```
     HELO client.com
     ```
   - Trả lời:  
     ```
     250 Hello client.com
     ```

3. **Xử lý lệnh MAIL FROM**
   - Nhận:  
     ```
     MAIL FROM:<a@b.com>
     ```
   - Kiểm tra email → nếu hợp lệ:  
     ```
     250 Sender OK
     ```
   - Nếu không hợp lệ:  
     ```
     550 Invalid sender address
     ```

4. **Xử lý lệnh RCPT TO**
   - Nhận:  
     ```
     RCPT TO:<c@d.com>
     ```
   - Kiểm tra email → nếu hợp lệ:  
     ```
     250 Recipient OK
     ```
   - Nếu không hợp lệ:  
     ```
     550 Invalid recipient address
     ```

5. **Xử lý lệnh DATA**
   - Nhận:  
     ```
     DATA
     ```
   - Trả lời:  
     ```
     354 Start mail input; end with <CRLF>.<CRLF>
     ```
   - Nhận nhiều dòng nội dung cho đến khi Client gửi `.`.

6. **Lưu nội dung email**
   - Sau khi nhận xong nội dung, server lưu email vào file `mails/mail_yyyyMMdd_HHmmss.txt`:
     ```
     From: a@b.com
     To: c@d.com
     Date: Mon Sep 16 21:30:25 ICT 2025
     Message:
     Xin chào, đây là email thử nghiệm.
     ```

7. **Kết thúc phiên làm việc**
   - Nhận:  
     ```
     QUIT
     ```
   - Trả lời:  
     ```
     221 Bye
     ```
   - Xóa session Client.

---

### 🔹Chức năng của Client (SMTP Client)

1. **Khởi động chương trình**
   - Hiển thị menu hướng dẫn nhập lệnh từng bước.

2. **Gửi lệnh HELO**
   - Người dùng nhập tên → gửi `HELO <tên>` đến Server.
   - Nhận phản hồi `250 Hello <tên>`.

3. **Gửi lệnh MAIL FROM**
   - Người dùng nhập email người gửi.
   - Gửi `MAIL FROM:<email>`.
   - Nhận phản hồi từ Server.

4. **Gửi lệnh RCPT TO**
   - Người dùng nhập email người nhận.
   - Gửi `RCPT TO:<email>`.
   - Nhận phản hồi từ Server.

5. **Gửi lệnh DATA + nội dung email**
   - Client gửi `DATA`.
   - Sau đó cho phép người dùng nhập nhiều dòng nội dung.
   - Kết thúc khi người dùng nhập dấu `.`.

6. **Gửi lệnh QUIT**
   - Client gửi `QUIT`.
   - Nhận phản hồi `221 Bye`.
   - Hiển thị:  
     ```
     ✅ Phiên SMTP đã kết thúc.
     ```

---

## 🏗 Kiến trúc hệ thống

## 🛠️ 3. Công nghệ sử dụng
Ngôn ngữ lập trình: Java 17
Thư viện:
java.net.ServerSocket, java.net.Socket (xử lý TCP).
BufferedReader, PrintWriter (gửi/nhận dữ liệu dạng text).
Giao thức: SMTP (Simple Mail Transfer Protocol).
Công cụ IDE: Eclipse / IntelliJ IDEA.
Môi trường chạy: Windows / Linux / macOS.