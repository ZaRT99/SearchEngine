package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "lemma", indexes = {@Index(name = "lemma_list", columnList = "lemma")})
@NoArgsConstructor
public class LemmaModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private SiteModel siteModelId;
    private String lemma;
    private int frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<IndexModel> indexModelList = new ArrayList<>();

    public LemmaModel(SiteModel siteModelId, String lemma, int frequency) {
        this.siteModelId = siteModelId;
        this.lemma = lemma;
        this.frequency = frequency;
    }
}
