package searchengine.parsers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexDto;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.services.interfaces.repository.IRepositoryIndex;
import searchengine.services.interfaces.repository.IRepositoryLemma;
import searchengine.services.interfaces.repository.IRepositoryPage;
import searchengine.services.interfaces.repository.IRepositorySite;
import searchengine.services.morphology.Morphology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Getter
@Slf4j
@RequiredArgsConstructor
public class IndexParser {
    private static final int processorCoreCount = Runtime.getRuntime().availableProcessors();
    private final IRepositorySite repositorySite;
    private final IRepositoryPage repositoryPage;
    private final IRepositoryLemma repositoryLemma;
    private final IRepositoryIndex repositoryIndex;
    private final Morphology morphology;
    private final LemmaParser lemmaParser;
    private final String url = "";
    private final SitesList sitesList;
    private List<IndexDto> config;

    public void startParsing(SiteModel site) {
        Iterable<PageModel> pageList = repositoryPage.findBySiteId(site);
        List<LemmaModel> lemmaList = repositoryLemma.findBySiteModelId(site);
        config = new ArrayList<>();
        final float RANK = 0.8f;
        for (PageModel page : pageList) {
            if (page.getCode() < 400) {
                long pageId = page.getId();
                String content = page.getContent();
                String title = clearCodeFromTag(content, "title");
                String body = clearCodeFromTag(content, "body");
                Map<String, Integer> titleSiteList = morphology.getLemmas(title);
                Map<String, Integer> bodySiteList = morphology.getLemmas(body);
                float totalRank = 0.0f;
                float titleRank;
                float bodyRank;
                for (LemmaModel lemma : lemmaList) {
                    long lemmaId = lemma.getId();
                    String keyWord = lemma.getLemma();
                    if (titleSiteList.containsKey(keyWord) || bodySiteList.containsKey(keyWord)) {
                        if (titleSiteList.get(keyWord) != null) {
                            titleRank = titleSiteList.get(keyWord);
                            totalRank += titleRank;
                        }
                        if (bodySiteList.get(keyWord) != null) {
                            bodyRank = bodySiteList.get(keyWord) * RANK;
                            totalRank += bodyRank;
                        }
                        config.add(new IndexDto(pageId, lemmaId, totalRank));
                    } else {
                        log.debug("Lemma not found");
                    }
                }
            } else {
                log.debug("Bad status code - " + page.getCode());
            }
        }
    }

    public List<IndexDto> getIndexList() {
        return config;
    }

    public String clearCodeFromTag(String content, String s) {
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select(s);
        String html = elements.stream().map(Element::html).collect(Collectors.joining());
        return Jsoup.parse(html).text();
    }
}
