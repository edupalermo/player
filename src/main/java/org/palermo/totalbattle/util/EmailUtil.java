package org.palermo.totalbattle.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtil {
    
    private static final String[] PATHS = {
            "/home/palermo/workspace/tokens/gmail.json", "/home/eduardo/tokens/gmail.json"
    };

    @SneakyThrows
    public static Optional<String> getVerificationCode() {

        Folder inbox = null;
        Store store = null;
        
        try {
            Properties props = new Properties();
            props.put("mail.imaps.host", "imap.gmail.com");
            props.put("mail.imaps.port", "993");
            props.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);

            store = session.getStore("imaps");

            ImapCredentials imapCredentials = readImapCredentials();
            
            store.connect(
                    imapCredentials.host,
                    imapCredentials.user,
                    imapCredentials.password
            );

            inbox = store.getFolder("INBOX");
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
                String verificationCode = extractVerificationCode(message);
    
                // Mark as read
                message.setFlag(Flags.Flag.SEEN, true);
                
                return Optional.ofNullable(verificationCode);
            }

            return Optional.empty();
        } finally {
            if (inbox != null) {
                inbox.close(false);
            }
            if (store != null) {
                store.close();
            }
        }
    }

    public static String findExistingCredentialFile() {
        return Arrays.stream(PATHS)
                .filter(path -> Files.exists(Path.of(path)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Cannot find a google credential to access google sheet!"));
    }
    
    public static ImapCredentials readImapCredentials() {
        try {
            return new ObjectMapper().readValue(
                    Path.of(findExistingCredentialFile()).toFile(),
                    ImapCredentials.class
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read IMAP credentials", e);
        }
    }    
    
    private static class ImapCredentials {
        public String host;
        public String user;
        public String password;
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
