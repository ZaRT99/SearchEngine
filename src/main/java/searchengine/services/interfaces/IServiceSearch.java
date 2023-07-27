package searchengine.services.interfaces;

import searchengine.dto.statistics.SearchDto;

import java.util.List;

public interface IServiceSearch {
    List<SearchDto> searchAllSites(String str, int offset, int limit);
    List<SearchDto> searchOneSite(String text, String str, int offset, int limit);
}
