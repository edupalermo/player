package org.palermo.totalbattle;

import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailTest {

/*
    public static void mainOld(String[] args) {
        doSomething();
    }

    @SneakyThrows
    private static void doSomething() {

        GoogleCredentials credentials = GoogleCredentials
                //.fromStream(new FileInputStream("/home/eduardo/tokens/email-reader-486911-fefc6217fd68.json"))
                //.fromStream(new FileInputStream("/home/eduardo/tokens/client_secret.json"))
                .fromStream(new FileInputStream("/home/eduardo/tokens/credentials.json"))
                .createScoped(Collections.singleton(GmailScopes.GMAIL_READONLY)) // GMAIL_MODIFY
                .createDelegated("edupalermo@gmail.com");

        Gmail gmail = new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("My Gmail Reader")
                .build();

        ListMessagesResponse response = gmail.users()
                .messages()
                .list("me")
                .setQ("is:unread")
                .execute();

        for (Message message : response.getMessages()) {

            Message fullMessage = gmail.users()
                    .messages()
                    .get("me", message.getId())
                    .setFormat("full")
                    .execute();

            System.out.println("Message ID: " + fullMessage.getId());

            for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                if ("Subject".equalsIgnoreCase(header.getName())) {
                    System.out.println("Subject: " + header.getValue());
                }

                if ("From".equalsIgnoreCase(header.getName())) {
                    System.out.println("From: " + header.getValue());
                }
            }
        }
    }

*/

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);

        Store store = session.getStore("imaps");

        store.connect(
                "imap.gmail.com",
                "edupalermo@gmail.com",
                "jgkr zzfs cqrs ftwr"
        );

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        
        SearchTerm unread = new FlagTerm(
                new Flags(Flags.Flag.SEEN),
                false
        );

        SearchTerm subject = new SubjectTerm("verification code");
        SearchTerm searchTerm = new AndTerm(unread, subject);
        
        // Find unread messages
        Message[] messages = inbox.search(searchTerm);
        

        for (Message message : messages) {
            System.out.println("From: " +
                    String.join(", ", addresses(message.getFrom())));
            System.out.println("Subject: [" + message.getSubject() + "]");
            System.out.println("Received Date: " + message.getReceivedDate());
            System.out.println("Code: [" + extractVerificationCode(message) + "]");


            // Mark as read
            message.setFlag(Flags.Flag.SEEN, true);
        }

        inbox.close(false);
        store.close();
    }

    private static String[] addresses(Address[] addresses) {
        if (addresses == null) {
            return new String[0];
        }

        String[] result = new String[addresses.length];

        for (int i = 0; i < addresses.length; i++) {
            result[i] = addresses[i].toString();
        }

        return result;
    }

    private static final Pattern VERIFICATION_CODE_PATTERN =
            Pattern.compile(
                    "Do not share this code with anyone!\\s*([A-Z0-9]{6})",
                    Pattern.CASE_INSENSITIVE
            );

    private static String extractVerificationCode(Message message) throws Exception {

        String body = getText(message);

        if (body == null) {
            return null;
        }

        Matcher matcher = VERIFICATION_CODE_PATTERN.matcher(body);

        return matcher.find()
                ? matcher.group(1)
                : null;
    }

    private static String getText(Part part) throws Exception {

        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }

        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                String text = getText(multipart.getBodyPart(i));

                if (text != null) {
                    return text;
                }
            }
        }

        return null;
    }    
}
