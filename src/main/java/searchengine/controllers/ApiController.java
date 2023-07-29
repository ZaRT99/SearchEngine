package searchengine.controllers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.SearchDto;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.responce.BadResponse;
import searchengine.dto.statistics.responce.OkResponse;
import searchengine.dto.statistics.responce.SearchResponse;
import searchengine.services.interfaces.IServiceIndexing;
import searchengine.services.interfaces.IServiceSearch;
import searchengine.services.interfaces.StatisticsService;
import searchengine.services.interfaces.repository.IRepositorySite;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    private final StatisticsService statisticsService;
    private final IServiceIndexing serviceIndexing;
    private final IRepositorySite repositorySite;
    private final IServiceSearch serviceSearch;

    public ApiController(StatisticsService statisticsService, IServiceIndexing serviceIndexing, IRepositorySite repositorySite, IServiceSearch serviceSearch) {
        this.statisticsService = statisticsService;
        this.serviceIndexing = serviceIndexing;
        this.repositorySite = repositorySite;
        this.serviceSearch = serviceSearch;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing() {
        if(serviceIndexing.indexingAllSites()){
            return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadResponse(false, "Индексация уже запущена"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing() {
        if(serviceIndexing.stopIndexing()){
            return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadResponse(false, "Индексация не запущена"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexingOnePage(@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            return new ResponseEntity<>(new BadResponse(false, "Страница не указана"), HttpStatus.BAD_REQUEST);
        } else {
            if (serviceIndexing.indexingOnePage(url)) {
                return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new BadResponse(false, "Данная страница находится за пределами сайтов,\n" +
                        "указанных в конфигурационном файле"), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (query.isEmpty()) {
            return new ResponseEntity<>(new BadResponse(false, "Задан пустой поисковый запрос"), HttpStatus.BAD_REQUEST);
        } else {
            List<SearchDto> searchData;
            if (!site.isEmpty()) {
                if (repositorySite.findByUrl(site) == null) {
                    return new ResponseEntity<>(new BadResponse(false, "Указанная страница не найдена"), HttpStatus.BAD_REQUEST);
                } else {
                    searchData = serviceSearch.searchOneSite(query, site, offset, limit);
                }
            } else {
                searchData = serviceSearch.searchAllSites(query, offset, limit);
            }

            return new ResponseEntity<>(new SearchResponse(true, searchData.size(), searchData), HttpStatus.OK);
        }
    }

}