package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.SitesList;
import searchengine.config.UserAgent;
import searchengine.dto.statistics.PageDto;

import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class IndexPage extends RecursiveTask<List<PageDto>> {
    private final String url;
    private final List<String> urlList;
    private final List<PageDto> pageDtoList;

    public IndexPage (String url, List<PageDto> pageDtoList, List<String> urlList) {
        this.url = url;
        this.pageDtoList = pageDtoList;
        this.urlList = urlList;
    }

    @Override
    protected List<PageDto> compute() {
        try {
            Thread.sleep(150);
            Document doc = getConnect(url);
            String html = doc.outerHtml();
            Connection.Response response = doc.connection().response();
            int status = response.statusCode();
            PageDto pageDto = new PageDto(url, html, status);
            pageDtoList.add(pageDto);
            Elements elements = doc.select("body").select("a");
            List<IndexPage> taskList = new ArrayList<>();
            for (Element el : elements) {
                String link = el.attr("abs:href");
                if (link.startsWith(el.baseUri())
                        && !link.equals(el.baseUri())
                        && !link.contains("#")
                        && !link.contains(".pdf")
                        && !link.contains(".jpg")
                        && !link.contains(".JPG")
                        && !link.contains(".png")
                        && !urlList.contains(link)) {
                    urlList.add(link);
                    IndexPage task = new IndexPage(link, pageDtoList, urlList);
                    task.fork();
                    taskList.add(task);
                }
            }
            taskList.forEach(ForkJoinTask::join);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return pageDtoList;
    }

    public Document getConnect(String url) {
        Document doc = null;
        try {
            Thread.sleep(150);
            doc = Jsoup.connect(url)
                    .userAgent(UserAgent.getUserAgent())
                    .referrer("http://www.google.com")
                    .get();
        } catch (Exception ex) {
            ex.getMessage();
        }
        return doc;
    }

}