package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.dto.statistics.LemmaDto;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.services.ClearCode;
import searchengine.services.interfaces.repository.IRepositoryPage;
import searchengine.services.morphology.IMorphology;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaParser {
    private final IRepositoryPage repositoryPage;
    private final IMorphology morphology;
    private List<LemmaDto> lemmaDtoList;

    public void start(SiteModel site) {
        lemmaDtoList = new CopyOnWriteArrayList<>();
        Iterable<PageModel> pageList = repositoryPage.findAll();
        TreeMap<String, Integer> lemmaList = new TreeMap<>();
        for (PageModel page : pageList) {
            String content = page.getContent();
            String title = ClearCode.clear(content, "title");
            String body = ClearCode.clear(content, "body");
            HashMap<String, Integer> titleList = morphology.getLemmas(title);
            HashMap<String, Integer> bodyList = morphology.getLemmas(body);
            Set<String> allWords = new HashSet<>();
            allWords.addAll(titleList.keySet());
            allWords.addAll(bodyList.keySet());
            for (String word : allWords) {
                int frequency = lemmaList.getOrDefault(word, 0) + 1;
                lemmaList.put(word, frequency);
            }
        }
        for (String lemma : lemmaList.keySet()) {
            Integer frequency = lemmaList.get(lemma);
            lemmaDtoList.add(new LemmaDto(lemma, frequency));
        }
    }

    public List<LemmaDto> getLemmaDtoList() {
        return lemmaDtoList;
    }

}
