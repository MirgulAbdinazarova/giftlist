package kg.peaksoft.giftlistb6.dto.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FriendProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isFriend;
    private String photo;
    private String country;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private int shoeSize;
    private String clothingSize;
    private String hobby;
    private String important;
    private List<HolidayGiftsResponse> wishResponses;
    private List<HolidayResponses> holidayResponses;
    private List<CharityResponse> charityResponses;
}
