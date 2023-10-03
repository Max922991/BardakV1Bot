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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
        text.add("Назад");
        cfg.add(1);
        data.add(CARWASH);
        int row = 0;
        for (int i = 8; i <= 18; i++) {
            text.add(i + ":00");
            data.add(TIME_ADD + i);
            if (row == 4) {
                cfg.add(row);
                row = 0;
            }
            row += 1;
        }
        if (row != 0) {
            cfg.add(row);
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
                true
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
        var order = orderRepo.findByClientAndRecord(client, true);
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
        orderRepo.deleteByClientAndRecord(client, true);

        Order order = Order.builder()
                .client(client)
                .record(true)
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

//
//    private BotApiMethod<?> setOrderService(CallbackQuery callbackQuery) {
//        var user = clientRepo.findById(callbackQuery.getFrom().getId()).orElseThrow();
//        var order = orderRepo.findByClientAndRecord(user, true);
//        Service service = serviceRepo.findByTittle(callbackQuery.getData());
//        if (!order.getServices().contains(service)) {
//            order.addService(service);
//        } else {
//            order.deleteService(service);
//        }
//        orderRepo.save(order);
//        return choosingAService(callbackQuery);
//    }
//
//
//    private BotApiMethod<?> washRecord(CallbackQuery callbackQuery) {
//        Order order = Order.builder()
//                .build();
//        orderRepo.save(order);
//        return methodFactory.getEditMessageText(
//                callbackQuery,
//                """
//                        Выберите день""",
//                keyboardFactory.getInlineKeyboard(
//                        getDate(),
//                        getDaysCfg(getDate()),
//                        List.of(DAY_1, DAY_2, DAY_3, DAY_4, DAY_5, DAY_6, DAY_7)
//                ));
//    }
//
//
//    private BotApiMethod<?> choosingATimeOfWashing(CallbackQuery callbackQuery) {
//        List<String> text = getTime();
//        text.add("Назад");
//        return methodFactory.getEditMessageText(
//                callbackQuery,
//                """
//                        Выберите время""",
//                keyboardFactory.getInlineKeyboard(
//                        // List.of("10:00", "12:30", "15:00", "17:30", "Назад"),
//                        text,
//                        List.of(4, 1),
//                        List.of(WASH1, WASH2, WASH3, WASH4, CARWASH)
//                )
//        );
//    }
//
//
//    private BotApiMethod<?> choosingAService(CallbackQuery callbackQuery) {
////        List<String> services = serviceRepo.findAll().stream()
////                .map(Service::getTittle)
////                .toList();
//        var user = clientRepo.findById(callbackQuery.getFrom().getId()).orElseThrow();
//        var order = orderRepo.findByClientAndRecord(user, true);
//        List<String> services = new ArrayList<>();
//        for (Service service: serviceRepo.findAll()) {
//            if (order.getServices().contains(service)) {
//                services.add("+ " + service.getTittle());
//            } else {
//                services.add(service.getTittle());
//            }
//        }
//
//        return methodFactory.getEditMessageText(
//                callbackQuery,
//                """
//                        Выберите услугу """,
//                keyboardFactory.getInlineKeyboard(
//                        services,
//                        List.of(3, 3, 2),
//                        List.of(COMPLEXWASH, DISCCLEANING, QUARTZAPPLICATION, BODYCLEANING, SCINLOTION, PLASTICLOTION, DAY_1, APPLY)
//                )
//        );
//    }
//
//
//    private static List<String> getDate() {
//        LocalDate currentDate = LocalDate.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
//        List<String> formattedDate = new ArrayList<>();
//        for (int i = 0; i < 7; i++) {
//            formattedDate.add(currentDate.format(formatter));
//            currentDate = currentDate.plusDays(1);
//        }
//        return formattedDate;
//    }
//
//    private List<String> getTime() {
//        LocalTime currentTime = LocalTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//        List<String> list = new ArrayList<>();
//
//        LocalTime[] availableTimes = {LocalTime.of(10, 0),
//                LocalTime.of(12, 30),
//                LocalTime.of(15, 0),
//                LocalTime.of(17, 30)};
//
//        for (LocalTime appointmentTime : availableTimes) {
//            if (currentTime.isBefore(appointmentTime)) {
//                String formattedTime = appointmentTime.format(formatter);
//                list.add(formattedTime);
//            }
//            if (currentTime.isAfter(appointmentTime)) {
//                list.add("Занято");
//            }
//        }
////todo Проверить наличие записи на каждое время в заказах
//        return list;
//    }
}



