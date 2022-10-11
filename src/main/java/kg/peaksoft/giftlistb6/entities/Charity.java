package kg.peaksoft.giftlistb6.entities;

import kg.peaksoft.giftlistb6.enums.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "charity")
@Getter
@Setter
@NoArgsConstructor
public class Charity {
    @Id
    @SequenceGenerator(name = "charity_seq", sequenceName = "charity_seq", allocationSize = 1)
    @GeneratedValue(generator = "charity_seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "charity")
    private List<Category> category;

    @ManyToOne
    private User reservoir;
    @ManyToOne
    private User user;

    @Enumerated
    @Column(name = "charity_status")
    private Status charityStatus;

    @Column(length = 10000)
    private String description;

    private String condition;

    @Column(length = 10000)
    private String image;

    @Column(name = "created_date")
    private LocalDate createdDate;


}
