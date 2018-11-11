package Global;

import org.jsoup.nodes.Document;

public class Jsoup {
    public static Document getDocument(String url) {
        Document doc = null;
        int attempts = 1;
        int maxAttempts = 3;
        boolean continueHttpCall = true;
        String userAgent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

        while (continueHttpCall) {
            try {
                // doc = org.jsoup.Jsoup.connect(url).userAgent(userAgent).timeout(30 * 1000).get();
                doc = org.jsoup.Jsoup.connect(url).timeout(30 * 1000).get();
                continueHttpCall = false;
            } catch (Exception e) {
                if (attempts++ == maxAttempts) {
                    continueHttpCall = false;
                    e.printStackTrace();
                } else {
                    System.out.println("Attempt number " + attempts + " to get " + url);
                }
            }
        }

        return doc;
    }
}