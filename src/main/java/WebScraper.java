import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class WebScraper {
    public static String URL = "https://www.comparetv.com.au/streaming-search-library/?ctvcp=1770";

    public static void main(String[] args) throws IOException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        HtmlPage page = webClient.getPage(URL);
        HtmlDivision htmlDivision;
        DomText loadMoreText = page.getFirstByXPath(".//div[@class=\"load-more-wrapper\"][@style=\"display: none;\"]/div/text()");

        // Check if all the shows are loaded
        while (loadMoreText == null) {
            htmlDivision = page.getFirstByXPath("//div[@class=\"load-more-wrapper\"]/div");
            page = htmlDivision.click();
            webClient.waitForBackgroundJavaScript(2000);
            loadMoreText = page.getFirstByXPath(".//div[@class=\"load-more-wrapper\"][@style=\"display: none;\"]/div/text()");
        }

        List<DomText> showNameDomTexts = page.getByXPath("//div[@class=\"search-content-item\"]/a/p/text()");
        List<DomAttr> showLinkDomAttributes = page.getByXPath("//div[@class=\"search-content-item\"]/a/@href");

        webClient.close();

        ArrayList<String> showNames = new ArrayList<>();
        ArrayList<String> showLinks = new ArrayList<>();

        for (DomText showNameDomText : showNameDomTexts) {
            String text = showNameDomText.toString();

            if (text != null && text.length() > 0) {
                showNames.add(text);
            }
        }

        for (DomAttr showLinkDomAttribute : showLinkDomAttributes) {
            String text = showLinkDomAttribute.getValue();

            if (text != null && text.length() > 0) {
                showLinks.add(text);
            }
        }

        System.out.println("Total Netflix shows: " + showNames.size());
        System.out.println("CSV output:");
        System.out.println("\"Title\",\"URL\"");

        Iterator<String> showNamesIterator = showNames.iterator();
        Iterator<String> showLinksIterator = showLinks.iterator();

        while (showNamesIterator.hasNext() && showLinksIterator.hasNext()) {
            System.out.println(String.format("\"%s\",\"%s\"", showNamesIterator.next(), showLinksIterator.next()));
        }
    }
}
