package searchengine.services.interfaces.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Repository
public interface IRepositoryPage extends JpaRepository<PageModel, Long> {

    long countBySiteId (SiteModel siteId);
    Iterable<PageModel> findBySiteId(SiteModel site);
    @Query(value = "SELECT p.* FROM Page p JOIN Word_index i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas", nativeQuery = true)
    List<PageModel> findByLemmas(@Param("lemmas") Collection<LemmaModel> lemmaListId);
}
