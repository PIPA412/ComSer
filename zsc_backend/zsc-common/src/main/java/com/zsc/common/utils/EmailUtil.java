package com.zsc.common.utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 邮件发送工具类（手动创建Session，不依赖Spring自动配置）
 */
@Component
public class EmailUtil {

    private final Session mailSession;

    public EmailUtil() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("1034001861@qq.com", "roockrywwfjsbfce");
            }
        });
    }

    /**
     * 发送HTML邮件
     */
    public boolean sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress("1034001861@qq.com"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject, "UTF-8");
            msg.setContent(content, "text/html;charset=UTF-8");
            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendArrearsReminder(String to, String ownerName, String billNo,
                                       String amount, String dueDate, String period) {
        String subject = "社区服务系统 - 费用催缴通知";
        String content = """
            <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <div style="background: #e6a23c; color: white; padding: 15px; border-radius: 8px 8px 0 0; text-align: center;">
                    <h2 style="margin: 0;">费用催缴通知</h2>
                </div>
                <div style="padding: 20px;">
                    <p>尊敬的 <strong>%s</strong> 住户：</p>
                    <p>您好！根据系统记录，您有以下费用尚未缴纳：</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 15px 0;">
                        <tr style="background: #f5f7fa;">
                            <td style="padding: 10px; border: 1px solid #e0e0e0;"><strong>账单编号</strong></td>
                            <td style="padding: 10px; border: 1px solid #e0e0e0;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 10px; border: 1px solid #e0e0e0;"><strong>账单周期</strong></td>
                            <td style="padding: 10px; border: 1px solid #e0e0e0;">%s</td>
                        </tr>
                        <tr style="background: #fef0f0;">
                            <td style="padding: 10px; border: 1px solid #e0e0e0;"><strong>欠费金额</strong></td>
                            <td style="padding: 10px; border: 1px solid #e0e0e0; color: #f56c6c; font-size: 18px; font-weight: bold;">¥ %s</td>
                        </tr>
                        <tr>
                            <td style="padding: 10px; border: 1px solid #e0e0e0;"><strong>截止日期</strong></td>
                            <td style="padding: 10px; border: 1px solid #e0e0e0;">%s</td>
                        </tr>
                    </table>
                    <p style="color: #999; font-size: 13px;">请您尽快登录社区服务系统完成缴费，逾期将产生滞纳金。</p>
                    <p style="color: #999; font-size: 13px;">如有疑问，请联系物业管理处。</p>
                </div>
                <div style="background: #f5f7fa; padding: 10px; text-align: center; border-radius: 0 0 8px 8px; color: #999; font-size: 12px;">
                    <p style="margin: 0;">社区服务管理系统 · 自动发送</p>
                </div>
            </div>
        """.formatted(ownerName, billNo, period, amount, dueDate);

        return sendHtmlMail(to, subject, content);
    }
}
