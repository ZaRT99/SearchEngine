package searchengine.model;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "site")
public class SiteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(name = "status_time", nullable = false)
    private Date statusTime;
    @Column(name = "last_error")
    private String lastError;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteId", cascade = CascadeType.ALL)
    protected List<PageModel> pageModelList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteModelId", cascade = CascadeType.ALL)
    protected List<LemmaModel> lemmaModelList = new ArrayList<>();
}
