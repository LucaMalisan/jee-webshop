package src.auth;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import src.model.UserEmailConfirmed;

@Log
public class AuthMailSender {

  public void sendMail(UserEmailConfirmed userEmailConfirmed, String baseURL)
      throws NamingException, MailjetException {
    MailjetClient client;
    Context env = (Context) new InitialContext().lookup("java:comp/env");

    client =
        new MailjetClient(
            (String) env.lookup("mailjet.apiKey"), (String) env.lookup("mailjet.secretKey"));
    MailjetRequest request =
        new MailjetRequest(Emailv31.resource)
            .property(
                Emailv31.MESSAGES,
                new JSONArray()
                    .put(
                        new JSONObject()
                            .put(
                                Emailv31.Message.FROM,
                                new JSONObject().put("Email", "luca.malisan@students.ffhs.ch"))
                            .put(
                                Emailv31.Message.TO,
                                new JSONArray()
                                    .put(
                                        new JSONObject()
                                            .put("Email", userEmailConfirmed.getEmail())))
                            .put(Emailv31.Message.SUBJECT, "Please confirm your email")
                            .put(
                                Emailv31.Message.TEXTPART,
                                "Thanks for your registration at JEE webshop")
                            .put(
                                Emailv31.Message.HTMLPART,
                                String.format(
                                    "Thanks for your registration at JEE webshop. Please confirm your email <a href=\"%s/confirm-email/%s\">here</a>",
                                    baseURL, userEmailConfirmed.getConfirmKey()))));
    MailjetResponse resp = client.post(request);
    Logger logger = Logger.getLogger(getClass().getName());
    logger.info(request.getBody());
    logger.info(resp.getRawResponseContent());
  }
}
