package id.customer.core.service;

import com.lowagie.text.DocumentException;
import id.customer.core.models.EmailDetails;
import id.customer.core.models.Result;
import id.customer.core.models.User;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Map;


public interface EmailService {
  String sendSimpleMail(EmailDetails details);

  String sendMailWithAttachment(EmailDetails details);
  ResponseEntity<Result> sendMailForgotPassword(EmailDetails details, User checkUserEmail, String urlendpoint);

  boolean sendRegisterMail(Map<String, String> filesUpload) throws IOException, MessagingException, DocumentException;
}
