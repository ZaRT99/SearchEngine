package searchengine.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.parsers.LemmaParser;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.responce.IndexingResponse;
import searchengine.model.SiteModel;
import searchengine.parsers.IndexParser;
import searchengine.services.interfaces.repository.IRepositoryIndex;
import searchengine.services.interfaces.repository.IRepositoryLemma;
import searchengine.services.interfaces.repository.IRepositoryPage;
import searchengine.services.interfaces.repository.IRepositorySite;
import searchengine.services.interfaces.IServiceIndexing;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static searchengine.model.Status.INDEXING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceIndexing implements IServiceIndexing {
    private static final int coreCount = Runtime.getRuntime().availableProcessors();
    private ExecutorService executorService;
    private final IRepositorySite repositorySite;
    private final IRepositoryPage repositoryPage;
    private final IRepositoryLemma repositoryLemma;
    private final IRepositoryIndex repositoryIndex;
    private final LemmaParser lemmaParser;
    private final IndexParser indexParser;
    private final SitesList sitesList;
    private boolean check = false;

    @Override
    public boolean indexingAllSites() {
        if (indexingRunning()) {
            log.debug("Indexing started");
            new IndexingResponse(false, "Идет индексация").getErrorMsg();
        } else {
            List<Site> siteList = sitesList.getSites();
            executorService = Executors.newFixedThreadPool(coreCount);
            for (Site site : siteList) {
                String url = site.getUrl();
                SiteModel siteModel = new SiteModel();
                siteModel.setName(site.getName());
                log.info("Парсинг сайта -> ".concat(site.getName()));
                executorService.submit(new SiteIndexRun(repositorySite, repositoryPage, repositoryLemma,
                        repositoryIndex, lemmaParser, indexParser, url, sitesList));
            }
        }
        executorService.shutdown();
        return check;
    }

    @Override
    public boolean indexingOnePage(String url) {
        List<Site> urlsList = sitesList.getSites();
        for (Site s: urlsList){
            if (s.getUrl().equals(url)){
                executorService = Executors.newFixedThreadPool(coreCount);
                executorService.submit(new SiteIndexRun(repositorySite, repositoryPage, repositoryLemma,
                        repositoryIndex, lemmaParser, indexParser, url, sitesList));
                executorService.shutdown();
                check = !check;
            }
        }
        return check;
    }

    @Override
    public boolean stopIndexing() {
        if (indexingRunning()){
            executorService.shutdown();
            check = !check;
        }
        return check;
    }

    private boolean indexingRunning() {
        repositorySite.flush();
        Iterable<SiteModel> siteList = repositorySite.findAll();
        for (SiteModel site : siteList) {
            if (site.getStatus() == INDEXING) {
                check = !check;
            }
        }
        return check;
    }
}
