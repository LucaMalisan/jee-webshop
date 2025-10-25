package src.auth;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.extern.java.Log;
import src.model.UserEmailConfirmed;
import javax.naming.Context;

@Log
public class AuthMailSender {

  public void sendMail(UserEmailConfirmed userEmailConfirmed, String baseURL)
      throws NamingException, MessagingException {
    Context env = (Context) new InitialContext().lookup("java:comp/env");

    String to = userEmailConfirmed.getEmail();
    String from = (String) env.lookup("email.address");

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                try {
                  return new PasswordAuthentication(from, (String) env.lookup("email.password"));
                } catch (NamingException e) {
                  throw new RuntimeException(e);
                }
              }
            });

    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    message.setSubject("Please confirm your email");
    message.setText(
        String.format(
            "Thanks for your registration at JEE webshop. Please confirm your email at %s/confirm-email/%s>here</a>",
            baseURL, userEmailConfirmed.getConfirmKey()));

    new Thread(
            () -> {
              try {
                Transport.send(message);
              } catch (Exception ignored) {
              }
            })
        .start();
  }
}
