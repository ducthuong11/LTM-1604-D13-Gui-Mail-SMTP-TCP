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
2.1 **Khởi động chương trình**
   - Hiển thị menu hướng dẫn nhập lệnh từng bước.

2.2 **Gửi lệnh HELO**
   - Người dùng nhập tên → gửi `HELO <tên>` đến Server.
   - Nhận phản hồi `250 Hello <tên>`.

2.3 **Gửi lệnh MAIL FROM**
   - Người dùng nhập email người gửi.
   - Gửi `MAIL FROM:<email>`.
   - Nhận phản hồi từ Server.

2.4 **Gửi lệnh RCPT TO**
   - Người dùng nhập email người nhận.
   - Gửi `RCPT TO:<email>`.
   - Nhận phản hồi từ Server.

2.5 **Gửi lệnh DATA + nội dung email**
   - Client gửi `DATA`.
   - Sau đó cho phép người dùng nhập nhiều dòng nội dung.
   - Kết thúc khi người dùng nhập dấu `.`.

2.6 **Gửi lệnh QUIT**
   - Client gửi `QUIT`.
   - Nhận phản hồi `221 Bye`.
   - Hiển thị:  
     ```
     ✅ Phiên SMTP đã kết thúc.
     ```

---

## 🛠️ 3. Công nghệ sử dụng
Ngôn ngữ lập trình: Java 17
Thư viện:
java.net.ServerSocket, java.net.Socket (xử lý TCP).
BufferedReader, PrintWriter (gửi/nhận dữ liệu dạng text).
Giao thức: SMTP (Simple Mail Transfer Protocol).
Công cụ IDE: Eclipse / IntelliJ IDEA.
Môi trường chạy: Windows / Linux / macOS.

## ⚙️ 4. Các bước cài đặt & Chạy ứng dụng
🛠️ 4.1. Yêu cầu hệ thống

Máy bạn cần có Java 17 (hoặc Java 8+ cũng được).
IDE: Eclipse, IntelliJ IDEA, hoặc chạy trực tiếp bằng cmd/terminal.

📥 4.2. Các bước chạy chương trình

1. Chạy Server

Trong Package Explorer, tìm file Server.java (hoặc tên file Server của bạn).
Nhấp chuột phải → Run As → Java Application
Console sẽ hiển thị:
✅ Server đang chạy trên cổng 9999

2. Chạy Client

Mở file Client.java

Nhấp chuột phải → Run As → Java Application

Console Client hiển thị menu nhập lệnh.

3. Gửi email mô phỏng

Theo thứ tự các lệnh:

HELO → nhập tên:

HELO Thuong


MAIL FROM → nhập email người gửi:

MAIL FROM:thuong@example.com


RCPT TO → nhập email người nhận:

RCPT TO:huong@example.com


DATA → nhập nội dung email nhiều dòng, kết thúc bằng .

DATA
Chao Hương,
Day la email thu nghiem.
.


QUIT → kết thúc phiên:

QUIT


Console Client hiển thị:

✅ Phiên SMTP đã kết thúc.

4. Kiểm tra email trên Server

Server sẽ in ra console tất cả email nhận được, ví dụ:

Email từ thuong@example.com đến huong@example.com:
Chao Hương,
Day la email thu nghiem.

##📞 5. Liên hệ

Email: ducthuong246ss@gmail.com

GitHub: ducthuong11
