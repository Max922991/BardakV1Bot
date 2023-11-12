package com.example.bardakv1bot.service.schedule;

import com.example.bardakv1bot.entity.Order;
import com.example.bardakv1bot.repository.OrderRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * @author nerzon
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleService {

    OrderRepo orderRepo;

    @Async
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDataBase1() {
        for (Order order : getOrders()) {
            if (getWeekDay().equals(order.getWeekDay()) && "10:00".equals(order.getTimeOfRecord())) {
                order.setCompleted(true);
                orderRepo.save(order);
            }
        }
    }

    @Async
    @Scheduled(cron = "0 30 12 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDataBase2() {
        for (Order order : getOrders()) {
            if (getWeekDay().equals(order.getWeekDay()) && "12:30".equals(order.getTimeOfRecord())) {
                order.setCompleted(true);
                orderRepo.save(order);
            }
        }
    }

    @Async
    @Scheduled(cron = "0 0 15 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDataBase3() {
        for (Order order : getOrders()) {
            if (getWeekDay().equals(order.getWeekDay()) && "15:00".equals(order.getTimeOfRecord())) {
                order.setCompleted(true);
                orderRepo.save(order);
            }
        }
    }

    @Async
    @Scheduled(cron = "0 30 17 * * ?")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDataBase4() {
        for (Order order : getOrders()) {
            if (getWeekDay().equals(order.getWeekDay()) && "17:30".equals(order.getTimeOfRecord())) {
                order.setCompleted(true);
                orderRepo.save(order);
            }
        }
    }

    private List<Order> getOrders() {
        return orderRepo.findAllByRecordAndCompleted(
                true, false
        );
    }

    private String getWeekDay() {
        LocalDateTime now = LocalDateTime.now();
        return now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }
}
