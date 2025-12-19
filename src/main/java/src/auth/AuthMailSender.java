package src.auth;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import lombok.extern.java.Log;
import src.model.User;

/**
 * Class to send account confirmation mail after first login
 */

@Log
public class AuthMailSender {

  public void sendMail(User user, String baseURL)
      throws MessagingException {
    String to = user.getEmail();
    String from = System.getenv("EMAIL_ADDRESS");

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    //authenticate with email account credentials (email + app-password)
    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(from, System.getenv("EMAIL_PASSWORD"));
              }
            });

    //set properties of e-mail
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    message.setSubject("Please confirm your email");

    //provide link to user containing the confirmation key stored in database
    message.setText(
        String.format(
            "Thanks for your registration at JEE webshop. Please confirm your email at %s/confirm-email/%s",
            baseURL, user.getConfirmKey()));

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
