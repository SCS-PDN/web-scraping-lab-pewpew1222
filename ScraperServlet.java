import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/scrape")
public class ScrapeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Session tracking for visit count
        HttpSession session = request.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) visitCount = 0;
        visitCount++;
        session.setAttribute("visitCount", visitCount);

        String url = request.getParameter("url");
        String[] selectedOptions = request.getParameterValues("option");

        if (url == null || url.trim().isEmpty()) {
            response.getWriter().println("<p>Please provide a valid URL.</p>");
            return;
        }

        Map<String, Object> scrapedResults = new LinkedHashMap<>();

        try {
            Document document = Jsoup.connect(url).get();

            if (selectedOptions != null) {
                for (String option : selectedOptions) {
                    switch (option) {
                        case "title":
                            scrapedResults.put("Title", document.title());
                            break;


                        case "links":
                            List<Map<String, String>> links = new ArrayList<>();
                            for (Element link : document.select("a[href]")) {
                                Map<String, String> linkData = new HashMap<>();
                                linkData.put("text", link.text());
                                linkData.put("href", link.absUrl("href"));
                                links.add(linkData);
                            }
                            scrapedResults.put("Links", links);
                            break;


                        case "images":
                            List<Map<String, String>> images = new ArrayList<>();
                            for (Element img : document.select("img[src]")) {
                                Map<String, String> imgData = new HashMap<>();
                                imgData.put("alt", img.attr("alt"));
                                imgData.put("src", img.absUrl("src"));
                                images.add(imgData);
                            }
                            scrapedResults.put("Images", images);
                            break;
                    }
                }
            }

            // store results in session
            request.getSession().setAttribute("scrapedData", scrapedResults);

            //convert to json
            Gson gson = new Gson();
            String jsonData = gson.toJson(scrapedResults);
            request.getSession().setAttribute("jsonData", jsonData);


            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            out.println("<html><head><title>Scraped Results</title></head><body>");

            out.println("<h2>Scraping Results</h2>");



            // Display visit count msg
            out.println("<p>You have visited this page " + visitCount + " times.</p>");

            // Display scraped results
            for (Map.Entry<String, Object> entry : scrapedResults.entrySet()) {
                out.println("<h3>" + entry.getKey() + "</h3>");

                if (entry.getValue() instance oof String) {
                    out.println("<p>" + entry.getValue() + "</p>");
                }

                else {
                    List<Map<String, String>> items = (List<Map<String, String>>) entry.getValue();
                    out.println("<table border='1'><tr><th>Text/Alt</th><th>URL</th></tr>");
                    for (Map<String, String> item : items) {
                        String label = item.getOrDefault("text", item.getOrDefault("alt", ""));

                        String link = item.getOrDefault("href", item.getOrDefault("src", ""));
                        out.println("<tr><td>" + label + "</td><td>" + link + "</td></tr>");
                    }
                    out.println("</table>");
                }
            }

            // Download CSV form
            out.println("<form method='GET' action='scrape'>");
            out.println("<input type='submit' name='download' value='Download CSV'>");
            out.println("</form>");

            out.println("</body></html>");

        } catch (IOException e) {
            response.getWriter().println("<p> Error fetchhing URL " + e.getMessage() + "</p>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String downloadRequest = request.getParameter("download");

        if (downloadRequest != null) {
            Object sessionData = request.getSession().getAttribute("scrapedData");

            if (sessionData == null) {
                response.getWriter().println("<p>No scraped data available to download.</p>");
                return;
            }

            Map<String, Object> scrapedData = (Map<String, Object>) sessionData;

            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=results.csv");

            PrintWriter writer = response.getWriter();

            for (Map.Entry<String, Object> entry : scrapedData.entrySet()) {
                writer.println(entry.getKey());

                if (entry.getValue() instanceof String) {
                    writer.println(entry.getValue());
                    writer.println();
                } else {

                    writer.println("Text/Alt,URL");
                    List<Map<String, String>> items = (List<Map<String, String>>) entry.getValue();
                    for (Map<String, String> item : items) {
                        String label = item.getOrDefault("text", item.getOrDefault("alt", ""));
                        String link = item.getOrDefault("href", item.getOrDefault("src", ""));
                        writer.println(label.replaceAll(",", " ") + "," + link);
                    }
                    writer.println();
                }
            }
        }
    }
}
