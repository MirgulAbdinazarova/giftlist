package kg.peaksoft.giftlistb6.db.services;

import kg.peaksoft.giftlistb6.db.models.*;
import kg.peaksoft.giftlistb6.db.repositories.*;
import kg.peaksoft.giftlistb6.dto.requests.WishRequest;
import kg.peaksoft.giftlistb6.dto.responses.HolidayResponse;
import kg.peaksoft.giftlistb6.dto.responses.InnerWishResponse;
import kg.peaksoft.giftlistb6.dto.responses.SimpleResponse;
import kg.peaksoft.giftlistb6.dto.responses.WishResponse;
import kg.peaksoft.giftlistb6.enums.NotificationType;
import kg.peaksoft.giftlistb6.enums.Status;
import kg.peaksoft.giftlistb6.exceptions.BadRequestException;
import kg.peaksoft.giftlistb6.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishService {

    private final WishRepository wishRepository;

    private final HolidayRepository holidayRepository;

    private final UserRepository userRepository;

    private final NotificationRepository notificationRepository;

    private final GiftRepository giftRepository;

    public User getAuthPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с таким  электронным адресом: %s не найден!", email)));
    }

    public WishResponse saveWish(WishRequest wishRequest) {
        Wish wish = mapToEntity(wishRequest);
        Holiday holiday = holidayRepository.findById(wishRequest.getHolidayId()).orElseThrow(
                () -> new NotFoundException("Не найден")
        );
        holiday.addWish(wish);
        wish.setHoliday(holiday);
        wish.setWishStatus(Status.WAIT);
        wishRepository.save(wish);
        User user = getAuthPrincipal();
        for (User friend : user.getFriends()) {
            Notification notification = new Notification();
            notification.setNotificationType(NotificationType.ADD_WISH);
            notification.setIsSeen(false);
            notification.setCreatedDate(LocalDate.now());
            notification.setUser(friend);
            notification.setFromUser(user);
            System.out.println(wish.getWishName());
            notification.setWish(wish);
            friend.setNotifications(List.of(notification));
            notificationRepository.save(notification);
        }
        log.info("Wish with id: {} successfully saved in the database", wish.getId());
        return mapToResponse(wish);
    }

    @Transactional
    public Wish mapToEntity(WishRequest wishRequest) {
        Wish wish = new Wish();
        User user = getAuthPrincipal();
        wish.setUser(user);
        wish.setWishName(wishRequest.getWishName());
        wish.setDescription(wishRequest.getDescription());
        Holiday holiday = holidayRepository.findById(wishRequest.getHolidayId())
                .orElseThrow(() -> new NotFoundException("Не найден"));
        if (!wishRequest.getDateOfHoliday().equals(holiday.getDateOfHoliday())) {
            throw new BadRequestException("Неверная дата");
        }
        wish.setDateOfHoliday(holiday.getDateOfHoliday());
        wish.setImage(wishRequest.getImage());
        wish.setLinkToGift(wishRequest.getLinkToGift());

        return wish;
    }

    public WishResponse mapToResponse(Wish wish) {
        WishResponse response = new WishResponse();
        response.setId(wish.getId());
        response.setWishName(wish.getWishName());
        response.setImage(wish.getImage());
        response.setHoliday(
                new HolidayResponse(wish.getHoliday().getName(), wish.getHoliday().getDateOfHoliday()));
        response.setWishStatus(wish.getWishStatus());
        return response;
    }

    public WishResponse update(Long id, WishRequest wishRequest) {
        Wish wish = getById(id);
        convertToUpdate(wish, wishRequest);
        wishRepository.save(wish);
        log.info("wish with id: {} successfully updated ", id);
        return mapToResponse(wish);
    }

    public SimpleResponse deleteWishById(Long id) {
        Wish wish = wishRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Желание с таким id: %s не найден!", id)));
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification n : notifications) {
            if (n.getWish() != null && n.getWish().equals(wish)) {
                notificationRepository.deleteById(n.getId());
            }
        }
        List<Gift> gifts = giftRepository.findAll();
        for (Gift g : gifts) {
            if (g.getWish().equals(wish)) {
                giftRepository.deleteById(g.getId());
            }
        }
        wishRepository.deleteById(id);
        log.info("Wish with id: {} successfully deleted",id);
        return new SimpleResponse(
                "Удалено",
                "Желание с таким id " + id + "удачно удалено");
    }

    public InnerWishResponse findById(Long id) {
        Wish wish = getById(id);
        return mapToInnerResponse(wish);
    }

    public List<WishResponse> findAll() {
        return convertAllToResponse(wishRepository.findAll());
    }

    public InnerWishResponse mapToInnerResponse(Wish wish) {
        InnerWishResponse innerWishResponse = new InnerWishResponse();
        innerWishResponse.setId(wish.getId());
        innerWishResponse.setWishName(wish.getWishName());
        innerWishResponse.setLinkToGift(wish.getLinkToGift());
        innerWishResponse.setHoliday(new HolidayResponse(wish.getHoliday().getName(), wish.getHoliday().getDateOfHoliday()));
        innerWishResponse.setDescription(wish.getDescription());
        return innerWishResponse;
    }

    public Wish convertToUpdate(Wish wish, WishRequest wishRequest) {
        wish.setWishName(wishRequest.getWishName());
        wish.setImage(wishRequest.getImage());
        wish.setLinkToGift(wishRequest.getLinkToGift());
        return wish;
    }

    public List<WishResponse> convertAllToResponse(List<Wish> wishes) {
        List<WishResponse> wishResponses = new ArrayList<>();
        for (Wish wish : wishes) {
            wishResponses.add(mapToResponse(wish));
        }
        return wishResponses;
    }

    private Wish getById(Long id) {
        return wishRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Желание id: " + id + " не найдено!"));
    }
}
