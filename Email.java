package LTM;

import java.io.Serializable;

public class Email implements Serializable {
    private String from;
    private String to;
    private String subject;
    private String body;

    public Email(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }

    @Override
    public String toString() {
        return "📧 Từ: " + from + "\n➡️ Đến: " + to + 
               "\n📌 Tiêu đề: " + subject + 
               "\n✉️ Nội dung: " + body;
    }
}
