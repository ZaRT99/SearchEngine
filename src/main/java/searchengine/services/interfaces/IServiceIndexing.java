package searchengine.services.interfaces;

public interface IServiceIndexing {
    boolean indexingAllSites();
    boolean indexingOnePage(String url);
    boolean stopIndexing();
}
