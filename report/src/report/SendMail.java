package report;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SendMail
{
    private static final String SMTP_HOST_NAME = "SSL0.OVH.NET";
    private static final String SMTP_AUTH_USER = "support@araka.mg";
    private static final String SMTP_AUTH_PWD  = "gatema-pass-93";
    private static final String SMTP_PORT  = "587";

    public static void main(String[] args) throws Exception{
    	String from = "support@araka.mg";
    	String to = "hrazakar@gatema.fr";
    	String subject = "Testing Subject";
    	String text = "This is message body";
    	String filename = "telo_COMPTE-RENDU_01-10-2016.html";
    	new SendMail().mailovh(from, to, subject, text, filename);
    }

    public void mailovh(String from, String to, String subject, String text, String filename) throws Exception{
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.enable", "false");   
        
        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(props, auth);
        // uncomment for debugging infos to stdout
        mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        //message.setContent("This is a test", "text/plain");
        message.setFrom(new InternetAddress(from));
        
		String recipient = to;
		String[] recipientList = recipient.split(",");
		InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
		int counter = 0;
		for (String recept : recipientList) {
		    recipientAddress[counter] = new InternetAddress(recept.trim());
		    counter++;
		}
		message.setRecipients(Message.RecipientType.TO, recipientAddress);

        //message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
     // Set Subject: header field
        message.setSubject(subject);
        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Now set the actual message
        messageBodyPart.setText(text);

        // Create a multipar message
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);
     // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        //String filename = "telo_COMPTE-RENDU_01-10-2016.html";
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        multipart.addBodyPart(messageBodyPart);
        
     // Send the complete message parts
        message.setContent(multipart);
        
        transport.connect();
        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
        System.out.println("Sent message successfully....");
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = SMTP_AUTH_USER;
           String password = SMTP_AUTH_PWD;
           return new PasswordAuthentication(username, password);
        }
    }  
}

