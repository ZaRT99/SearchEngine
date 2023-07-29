package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.parsers.LemmaParser;
import searchengine.parsers.PageParser;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.IndexDto;
import searchengine.dto.statistics.LemmaDto;
import searchengine.dto.statistics.PageDto;
import searchengine.model.*;
import searchengine.parsers.IndexParser;
import searchengine.services.interfaces.repository.IRepositoryIndex;
import searchengine.services.interfaces.repository.IRepositoryLemma;
import searchengine.services.interfaces.repository.IRepositoryPage;
import searchengine.services.interfaces.repository.IRepositorySite;


import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@RequiredArgsConstructor
public class SiteIndexRun implements Runnable {

    private static final int coreCount = Runtime.getRuntime().availableProcessors();
    private final IRepositorySite repositorySite;
    private final IRepositoryPage repositoryPage;
    private final IRepositoryLemma repositoryLemma;
    private final IRepositoryIndex repositoryIndex;
    private final LemmaParser lemmaParser;
    private final IndexParser indexParser;
    private final String url;
    private final SitesList sitesList;

    @Override
    public void run() {
        if (repositorySite.findByUrl(url) != null) {
            log.info("Идет удаление данный сайта ".concat(url));
            SiteModel site = repositorySite.findByUrl(url);
            site.setStatus(Status.INDEXING);
            site.setName(getSiteName());
            site.setStatusTime(new Date());
            repositorySite.saveAndFlush(site);
            repositorySite.delete(site);
        }
        log.info("Запущена индексация сайта ".concat(url).concat(" ").concat(getSiteName()));
        SiteModel site = new SiteModel();
        site.setUrl(url);
        site.setName(getSiteName());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        repositorySite.flush();
        repositorySite.save(site);
        try {
            List<PageDto> pageDtoList = getPageDtoList();
            if (!Thread.interrupted()) {
                List<PageDto> pages = pageDtoList;
                List<PageModel> pageList = new CopyOnWriteArrayList<>();
                SiteModel siteModel = repositorySite.findByUrl(url);
                for (PageDto page : pages) {
                    int start = page.getUrl().indexOf(url) + url.length();
                    String pageFormat = page.getUrl().substring(start);
                    pageList.add(new PageModel(siteModel, pageFormat, page.getCode(), page.getContent()));
                }
                repositoryPage.flush();
                repositoryPage.saveAll(pageList);
            } else {
                throw new InterruptedException();
            }
            getLemmas();
            indexingWords();

        } catch (InterruptedException e) {
            errorSite();
        }
    }

    private List<PageDto> getPageDtoList() throws InterruptedException {
        if (!Thread.interrupted()) {
            String urlSrt = url + "/";
            List<PageDto> pageDto = new Vector<>();
            List<String> urlList = new Vector<>();
            ForkJoinPool forkJoinPool = new ForkJoinPool(coreCount);
            List<PageDto> pages = forkJoinPool.invoke(new PageParser(urlSrt, pageDto, urlList));
            return new CopyOnWriteArrayList<>(pages);
        } else throw new InterruptedException();
    }

    private String getSiteName() {
        List<Site> sites = sitesList.getSites();
        for (Site map : sites) {
            if (map.getUrl().equals(url)) {
                return map.getName();
            }
        }
        return "";
    }

    private void errorSite() {
        List<SiteModel> sites = repositorySite.findAll();
        for (SiteModel s: sites) {
            if (s.getStatus() == Status.INDEXING) {
                s.setLastError("Индексация остановлена");
                s.setStatus(Status.FAILED);
                s.setStatusTime(new Date());
                repositorySite.save(s);
                String urlStr = s.getUrl();
                log.info("Индексация остановлена - " + urlStr);
            }
        }
    }

    private void getLemmas() {
        if (!Thread.interrupted()) {
            SiteModel siteModel = repositorySite.findByUrl(url);
            siteModel.setStatusTime(new Date());
            lemmaParser.start(siteModel);
            List<LemmaDto> lemmaDtoList = lemmaParser.getLemmaDtoList();
            List<LemmaModel> lemmaList = new CopyOnWriteArrayList<>();

            for (LemmaDto lemmaDto : lemmaDtoList) {
                lemmaList.add(new LemmaModel(siteModel, lemmaDto.getLemma(), lemmaDto.getFrequency()));
            }
            repositoryLemma.flush();
            repositoryLemma.saveAll(lemmaList);
        } else {
            throw new RuntimeException();
        }
    }

    private void indexingWords() throws InterruptedException {
        if (!Thread.interrupted()) {
            SiteModel site = repositorySite.findByUrl(url);
            indexParser.startParsing(site);
            List<IndexDto> indexDtoList = new CopyOnWriteArrayList<>(indexParser.getIndexList());
            List<IndexModel> indexList = new CopyOnWriteArrayList<>();
            site.setStatusTime(new Date());
            for (IndexDto indexDto : indexDtoList) {
                PageModel page = repositoryPage.getById(indexDto.getPageID());
                LemmaModel lemma = repositoryLemma.getById(indexDto.getLemmaID());
                indexList.add(new IndexModel(page, lemma, indexDto.getRank()));
            }
            repositoryIndex.flush();
            repositoryIndex.saveAll(indexList);
            log.info("Индексация завершена - " + url);
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXED);
            repositorySite.save(site);

        } else {
            throw new InterruptedException();
        }
    }
}
