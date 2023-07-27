package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.catalina.LifecycleState;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "page", indexes = {@Index(name = "path_list", columnList = "path")})
@Getter
@Setter
@NoArgsConstructor
public class PageModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private SiteModel siteId;
    @Column(columnDefinition = "VARCHAR(515)", length = 1000, nullable = false)
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<IndexModel> indexModelList = new ArrayList<>();
    public PageModel(SiteModel siteId, String path, int code, String content) {
        this.siteId = siteId;
        this.path = path;
        this.code = code;
        this.content = content;
    }
}

