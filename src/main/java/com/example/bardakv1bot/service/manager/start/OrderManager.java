package com.example.bardakv1bot.service.manager.start;

import com.example.bardakv1bot.entity.Order;
import com.example.bardakv1bot.entity.Service;
import com.example.bardakv1bot.factory.AnswerMethodFactory;
import com.example.bardakv1bot.factory.KeyboardFactory;
import com.example.bardakv1bot.repository.ClientRepo;
import com.example.bardakv1bot.repository.OrderRepo;
import com.example.bardakv1bot.repository.ServiceRepo;
import com.example.bardakv1bot.service.manager.AbstractManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.example.bardakv1bot.data.CallbackData.*;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderManager extends AbstractManager {

    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final ClientRepo clientRepo;
    final ServiceRepo serviceRepo;
    final OrderRepo orderRepo;


    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }


    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    @Transactional
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String queryData = callbackQuery.getData();
        String keyWord = queryData.split("_")[0];
        if ("service".equals(keyWord)) {
            return addService(callbackQuery, queryData.split("_")[1]);
        }
        if ("time".equals(keyWord)) {
            return chooseTime(callbackQuery);
        }
        switch (queryData) {
            case CARWASH -> {
                return washRecord(callbackQuery);
            }
            case DAY_1, DAY_2, DAY_3, DAY_4, DAY_5, DAY_6, DAY_7 -> {
                return chooseService(callbackQuery, true);
            }
//            case WASH1, WASH2, WASH3, WASH4 -> {
//                return choosingAService(callbackQuery);
//            }
//            case COMPLEXWASH -> {
//                return setOrderService(callbackQuery);
//            }
        }
        return null;
    }

    private BotApiMethod<?> chooseTime(CallbackQuery callbackQuery) {
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<String> timeList = new ArrayList<>(List.of("10:00", "12:30", "15:00", "17:30"));

        text.add("Назад");
        cfg.add(1);
        data.add(CARWASH);
        var user = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        var order = orderRepo.findByClientAndRecord(user, false);
        List<Order> orders = orderRepo.findAllByRecordAndCompleted(true, false);
        String weekDay = order.getWeekDay();

        for (Order check: orders) {
            if (timeList.contains(check.getTimeOfRecord()) && weekDay.equals(check.getWeekDay())) {
                timeList.remove(check.getTimeOfRecord());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equals(order.getWeekDay())) {
            int minutes = now.getHour() * 60 + now.getMinute();
            int row = 0;
            for (String time: timeList) {
                if (Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]) >= minutes) {
                    text.add(time);
                    data.add("time_" + time);
                    row += 1;
                }
            }
            if (row != 0) {
                cfg.add(row);
            }
        } else {
            for (String time: timeList) {
                text.add(time);
                data.add("time_" + time);
            }
            cfg.add(timeList.size());
        }

        text.add("Далее");
        cfg.add(1);
        data.add("next_step");
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете время",
                keyboardFactory.getInlineKeyboard(
                        text, cfg, data
                )
        );
    }

    private BotApiMethod<?> addService(CallbackQuery callbackQuery, String id) {
        var service = serviceRepo.findById(UUID.fromString(id)).orElseThrow();
        var order = orderRepo.findByClientAndRecord(
                clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow(),
                false
        );
        if (order.getServices() != null && order.getServices().contains(service)) {
            order.deleteService(service);
        } else {
            order.addService(service);
        }

        orderRepo.save(order);
        return chooseService(callbackQuery, false);
    }
    private BotApiMethod<?> chooseService(CallbackQuery callbackQuery, boolean flag) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        var order = orderRepo.findByClientAndRecord(client, false);
        if (flag) {
            Integer dayNumber = Integer.parseInt(callbackQuery.getData().split("_")[1]);
            order.setWeekDay(DayOfWeek.of(dayNumber).getDisplayName(TextStyle.FULL, Locale.ROOT));
            orderRepo.save(order);
        }
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете услугу",
                getServicesKeyboard(order)
        );
    }

    private InlineKeyboardMarkup getServicesKeyboard(Order order) {
        List<Service> services = serviceRepo.findAll();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        List<String> data = new ArrayList<>();
        int row = 0;
        for (Service service: services) {
            if (order.getServices().contains(service)) {
                text.add("✅" + service.getTittle());
            } else {
                text.add(service.getTittle());
            }
            data.add("service_" + service.getId());
            if (row == 3) {
                cfg.add(row);
                row = 0;
            }
            row += 1;
        }
        if (row != 0) {
            cfg.add(row);
        }
        cfg.add(2);
        text.add("Назад");
        text.add("Далее");
        data.add(CARWASH);
        data.add(TIME);
        return keyboardFactory.getInlineKeyboard(
                text, cfg, data
        );
    }

    private BotApiMethod<?> washRecord(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        orderRepo.deleteByClientAndRecord(client, false);

        Order order = Order.builder()
                .client(client)
                .record(false)
                .build();
        orderRepo.save(order);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете день!",
                getDaysKeyboard()
        );
    }

    private InlineKeyboardMarkup getDaysKeyboard() {
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        int dayNumber = LocalDateTime.now().getDayOfWeek().getValue();
        for (int day = dayNumber; day <= 7; day ++) {
            text.add(DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            data.add("day_" + day);
        }
        for (int day = 1; day < dayNumber; day ++) {
            text.add(DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            data.add("day_" + day);
        }
        return keyboardFactory.getInlineKeyboard(
                text,
                List.of(3, 3, 1),
                data
        );
    }

}



