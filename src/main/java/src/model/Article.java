package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "article")
public class Article implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "sellingPrice")
    private Double sellingPrice;

    @Column(name = "listPrice")
    private Double listPrice;

    @Column(name = "available")
    private Boolean available;

    @Column(name = "category")
    private String category;

    @OneToMany(mappedBy = "article")
    private List<ArticleImage> imageList;
}
