package src.auth;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.extern.java.Log;
import src.model.User;
import javax.naming.Context;

/**
 * Class to send account confirmation mail after first login
 */

@Log
public class AuthMailSender {

  public void sendMail(User user, String baseURL)
      throws NamingException, MessagingException {
    Context env = (Context) new InitialContext().lookup("java:comp/env");

    String to = user.getEmail();
    String from = (String) env.lookup("email.address");

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
                try {
                  return new PasswordAuthentication(from, (String) env.lookup("email.password"));
                } catch (NamingException e) {
                  throw new RuntimeException(e);
                }
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
            "Thanks for your registration at JEE webshop. Please confirm your email at %s/confirm-email/%s>here</a>",
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
