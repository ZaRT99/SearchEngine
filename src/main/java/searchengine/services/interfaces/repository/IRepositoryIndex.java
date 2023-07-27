package searchengine.services.interfaces.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface IRepositoryIndex extends JpaRepository<IndexModel, Long> {
    @Query(value = "select * from Word_index where Word_index.lemma_id in :lemmas and Word_index.page_id in :pages", nativeQuery = true)
    List<IndexModel> findByPageAndLemmas(@Param("lemmas") List<LemmaModel> lemmaList,
                                         @Param("pages") List<PageModel> pages);

    List<IndexModel> findByLemmaId (long lemmaId);
    List<IndexModel> findByPageId (long pageId);
    IndexModel findByLemmaIdAndPageId (long lemmaId, long pageId);
}
