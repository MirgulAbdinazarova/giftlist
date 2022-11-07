package kg.peaksoft.giftlistb6.db.models;

import kg.peaksoft.giftlistb6.dto.requests.MailingListRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mailing_list")
@Getter
@Setter
@NoArgsConstructor
public class MailingList {

    @Id
    @SequenceGenerator(name = "mailingList_gen", sequenceName = "mailingList_gen", allocationSize = 1)
    @GeneratedValue(generator = "mailingList_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String photo;

    @Column(length = 10000)
    private String text;


    private LocalDateTime createDate;

    public MailingList(MailingListRequest request) {
        this.name = request.getTopic();
        this.text = request.getText();
        this.photo = request.getPhoto();
    }
}
