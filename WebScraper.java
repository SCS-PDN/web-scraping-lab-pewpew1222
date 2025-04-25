import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class WebScraper {
    public static void main(String[] args) throws IOException {
        final String url = "https://www.bbc.com";

        Document doc= Jsoup.connect(url).get();

        System.out.println("Title of webpage : " + doc.title());

        System.out.println(" \nAll headings : ");
        for (int i=1;i<=6;i++){
            Elements headings = doc.select("h" +i);
            for (Element heading : headings) {
                System.out.println("h" + i + ":" + heading.text());
            }
        }

        System.out.println("\nAll links : ");
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            System.out.println(link.text() + "-" + link.attr("abs:href"));
        }


        List<Map<String, String>> newsList= new ArrayList<>();
        Elements articles = doc.select ("a:has(h3), a:has(h2)");

        for( Element article : articles){
            String headline= article.text();
            String link = article.absUrl("href");

            if(!link.isEmpty()) {
                try{Document articleDoc = Jsoup.connect(link).get();
                    String pubDate = articleDoc.select("time").text();
                    String author = articleDoc.select("[rel=author], .byline__name").text();

                    Map<String, String> newsItem = new HashMap<>();
                    newsItem.put("Headline", headline);
                    newsItem.put("Date", pubDate);
                    newsItem.put("Author", author);
                    newsList.add(newsItem);
                } catch (IOException e) {
                    System.out.println("Error of fetcching the article: " + link);
                }
            }
        }
