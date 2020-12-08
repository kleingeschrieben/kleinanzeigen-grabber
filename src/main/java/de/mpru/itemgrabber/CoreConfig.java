package de.mpru.itemgrabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Configuration
@EnableScheduling
public class CoreConfig {
    @Value("${mail.host}")
    private String mailHost;
    @Value("${mail.port}")
    private int mailPort;
    @Value("${mail.user}")
    private String mailUser;
    @Value("${mail.pass}")
    private String mailPass;
    @Value("${mail.smtpauth.enable}")
    private String mailSmtpAuth;
    @Value("${mail.starttls.enable}")
    private String mailStarttls;
    @Value("${mail.receiver}")
    private String mailReceiver;

    private boolean hasInitData = false;

    private MailHandler mail;

    private static final Logger logger = LoggerFactory.getLogger(CoreConfig.class);

    private Controller con = new Controller();

    @Scheduled (cron = "${system.cron}")
    public void startProcess() {
        File links = new File("links.txt");
        try (Scanner fileReader = new Scanner(links)){
            while (fileReader.hasNextLine()) {
                String url = fileReader.nextLine();
                logger.info("Checking: {} for new Items...", url);
                List<Date> dates = con.parseBody(url);
                boolean sendAlert = con.compareDates(dates, url, hasInitData);
                if (sendAlert) con.alertNewItem(url, mail);
            }
        } catch (FileNotFoundException e) {
            logger.error("Problem while loading links.txt...", e);
        }

        if (hasInitData == false) {
            logger.info("Successfully grabbed init data");
            mail = new MailHandler(mailHost, mailPort, mailUser, mailPass, mailSmtpAuth, mailStarttls, mailReceiver);
            hasInitData = true;
        }
    }
}
