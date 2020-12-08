package de.mpru.itemgrabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Controller {

    private Map<String, Date> lastNewArticle = new HashMap<>();

    private RestHandler rest = new RestHandler();
    private static final Logger logger = LoggerFactory.getLogger(RestHandler.class);

    public List<Date> parseBody(String ebayUrl) {
        logger.info("Start parsing retrived body of: {}", ebayUrl);
        if (!lastNewArticle.containsKey(ebayUrl)) {
            try {
                logger.debug("No init data found set init date to 01.01.2000 for search query: {]", ebayUrl);
                lastNewArticle.put(ebayUrl, new SimpleDateFormat("dd.MM.yyyy").parse("01.01.2000"));
            } catch (ParseException e) {
                logger.error("Failed to parse date...", e);
            }
        }
        String htmlBody = rest.sendGetRequest(ebayUrl);
        BufferedReader bufReader = new BufferedReader(new StringReader(htmlBody));
        boolean nextLine = false;
        List<Date> dates = new ArrayList<>();
        String line=null;
        while(true) {
            try {
                if (!((line=bufReader.readLine()) != null)) break;
            } catch (IOException e) {
                logger.error("Failed to read body...", e);
            }
            if (nextLine) {
                String cleanedUpLine = line.replaceAll("\\s{2,}", "").replace("</div>", "");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDateTime now = LocalDateTime.now();

                if (line.contains("<a")) continue;

                try {
                    if (cleanedUpLine.contains("Heute")) {
                        cleanedUpLine = cleanedUpLine.replace("Heute,", dtf.format(now));
                        dates.add(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(cleanedUpLine));
                    } else if (cleanedUpLine.contains("Gestern")) {
                        cleanedUpLine = cleanedUpLine.replace("Gestern,", dtf.format(now.minusDays(1)));
                        dates.add(new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(cleanedUpLine));
                    } else if (!cleanedUpLine.isEmpty()){
                        dates.add(new SimpleDateFormat("dd.MM.yyyy").parse(cleanedUpLine));
                    }
                } catch (ParseException e) {
                    logger.error("Problem while parsing: {} to date...", line, e);
                }
                nextLine = false;
            } else if (line.contains("addon")) {
                nextLine = true;
            }
        }

        return dates;
    }

    public boolean compareDates(List<Date> dates, String url, boolean hasInitData) {
        boolean newItemFound = false;
        for (int i = dates.size() - 1; i >= 0; i--) {
            if (lastNewArticle.get(url).before(dates.get(i))) {
                newItemFound = true;
                lastNewArticle.put(url, dates.get(i));
            }
        }

        if (hasInitData && newItemFound) return true;
        else return false;
    }

    public void alertNewItem(String url, MailHandler mail) {
        logger.info("New Articles found! Sending information to: {}", mail.getMailReceiver());
        mail.sendSimpleMessage(mail.getMailReceiver(), "NEUER ARTIKEL GEFUNDEN [eBay Kleinanzeigen Grabber by MPRU]", "Es wurde ein oder mehrere neue Artikel gefunden!\nLink zur eBay kleinanzeigen Suche:\n" + url, mail.getMailUser());
    }

}
