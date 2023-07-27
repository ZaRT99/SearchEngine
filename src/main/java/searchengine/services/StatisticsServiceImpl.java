package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.services.interfaces.StatisticsService;
import searchengine.services.interfaces.repository.IRepositoryLemma;
import searchengine.services.interfaces.repository.IRepositoryPage;
import searchengine.services.interfaces.repository.IRepositorySite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final IRepositorySite repositorySite;
    private final IRepositoryPage repositoryPage;
    private final IRepositoryLemma repositoryLemma;

    private TotalStatistics getTotal() {
        Long sites = repositorySite.count();
        Long pages = repositoryPage.count();
        Long lemmas = repositoryLemma.count();
        return new TotalStatistics(sites, pages, lemmas, true);
    }

    private DetailedStatisticsItem getDetailed(SiteModel site) {
        String url = site.getUrl();
        String name = site.getName();
        Status status = site.getStatus();
        Date statusTime = site.getStatusTime();
        String error = site.getLastError();
        long pages = repositoryPage.countBySiteId(site);
        long lemmas = repositoryLemma.countBySiteModelId(site);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

    private List<DetailedStatisticsItem> getDetailedList() {
        List<SiteModel> siteList = repositorySite.findAll();
        List<DetailedStatisticsItem> result = new ArrayList<>();
        for (SiteModel site : siteList) {
            DetailedStatisticsItem item = getDetailed(site);
            result.add(item);
        }
        return result;
    }


    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = getTotal();
        List<DetailedStatisticsItem> list = getDetailedList();
        return new StatisticsResponse(true, new StatisticsData(total, list));
    }
}
