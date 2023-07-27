package searchengine.services.morphology;

import java.util.HashMap;
import java.util.List;

public interface IMorphology {
    HashMap<String, Integer> getLemmas(String content);
    List<String> getLemma(String word);
    List<Integer> findLemmaIndex(String content, String lamma);
}
