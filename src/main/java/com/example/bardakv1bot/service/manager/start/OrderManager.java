package com.example.bardakv1bot.service.manager.start;

import com.example.bardakv1bot.data.Callback;
import com.example.bardakv1bot.entity.Action;
import com.example.bardakv1bot.entity.Client;
import com.example.bardakv1bot.entity.Order;
import com.example.bardakv1bot.entity.Service;
import com.example.bardakv1bot.factory.AnswerMethodFactory;
import com.example.bardakv1bot.factory.KeyboardFactory;
import com.example.bardakv1bot.repository.ClientRepo;
import com.example.bardakv1bot.repository.OrderRepo;
import com.example.bardakv1bot.repository.PhoneRepo;
import com.example.bardakv1bot.repository.ServiceRepo;
import com.example.bardakv1bot.service.manager.AbstractManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.example.bardakv1bot.data.Callback.*;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderManager extends AbstractManager {

    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final ClientRepo clientRepo;
    final ServiceRepo serviceRepo;
    final OrderRepo orderRepo;
    final PhoneManager phoneManager;
    final PhoneRepo phoneRepo;

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
        String[] words = callbackQuery.getData().split("_");
        switch (words.length) {
            case 1 -> {
                return washRecord(callbackQuery);
            }
            case 2 -> {
                switch (words[1]) {
                    case "enter" -> {
                        return saveNewPhoneNumber(callbackQuery);
                    }
                    case "time" -> {
                        return chooseTime(callbackQuery);
                    }
                    case "service" -> {
                        return checkForTime(callbackQuery);
                    }
                    case "finish" -> {
                        try {
                            return finishOrder(callbackQuery, bot);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            case 3 -> {
                switch (words[1]) {
                    case "next" -> {
                        return nextStep(callbackQuery);
                    }
                    case "time" -> {
                        return putTime(callbackQuery);
                    }
                    case "close" -> {
                        return closeOrder(callbackQuery, words[2]);
                    }
                    case "service" -> {
                        return addService(callbackQuery, words[2]);
                    }
                    case "day" -> {
                        return putDay(callbackQuery);
                    }
                }
            }
        }
        return null;
    }
    private BotApiMethod<?> checkForTime(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        var order = orderRepo.findByClientAndRecord(client, false);
        if (order.getTimeOfRecord() != null && !order.getTimeOfRecord().isEmpty()) {
            return chooseService(callbackQuery);
        }
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .text("Вы обязаны указать время!")
                .build();
    }
    private BotApiMethod<?> closeOrder(CallbackQuery callbackQuery, String s) {
        Order order = orderRepo.findById(Long.valueOf(s)).orElseThrow();
        order.setCompleted(true);
        orderRepo.save(order);
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .text("Заказ завершен!")
                .build();
    }

    private BotApiMethod<?> saveNewPhoneNumber(CallbackQuery callbackQuery) {
        Long id = callbackQuery.getMessage().getChatId();
        Client client = clientRepo.findById(id).orElseThrow();
        client.setAction(Action.FREE);

        String phone = phoneRepo.findByClient(client).getPhoneNumber();
        client.setPhoneNumber(phone);
        clientRepo.save(client);

        return nextStep(callbackQuery);
    }

    private BotApiMethod<?> putTime(CallbackQuery callbackQuery) {
        Client client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        Order order = orderRepo.findByClientAndRecord(client, false);
        order.setTimeOfRecord(callbackQuery.getData().split("_")[2]);
        orderRepo.save(order);
        return chooseTime(callbackQuery);
    }

    private BotApiMethod<?> nextStep(CallbackQuery callbackQuery) {
        Client client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        String phoneNumber = client.getPhoneNumber();
        if (phoneNumber != null) {
            return methodFactory.getEditMessageText(
                    callbackQuery,
                    "Вы хотите оставить текущий номер или изменить его?",
                    keyboardFactory.getInlineKeyboard(
                            List.of(phoneNumber, "Ввести другой"),
                            List.of(2),
                            List.of(order_finish.name(), phone_.name())
                    )
            );
        }
        return phoneManager.mainMenu(callbackQuery, client, null);
    }

    private BotApiMethod<?> finishOrder(CallbackQuery callbackQuery, Bot bot) throws TelegramApiException {
        Long id = callbackQuery.getMessage().getChatId();
        Client client = clientRepo.findById(id).orElseThrow();
        Order order = orderRepo.findByClientAndRecord(client, false);
        order.setRecord(true);
        order.setCompleted(false);
        orderRepo.save(order);
        bot.execute(
                methodFactory.getSendMessage(
                        660883009L,
                        getOrderInfo(order) + "\n" + "order ID " + order.getId(),
                        keyboardFactory.getInlineKeyboard(
                                List.of("Завершить"),
                                List.of(1),
                                List.of(order_close_.name() + order.getId())
                        )
                )
        );
        return methodFactory.getEditMessageText(
                callbackQuery,
                getOrderInfo(order),
                null
        );
    }


    private String getOrderInfo(Order order) {
        List<Service> services = order.getServices();
        StringBuilder sb = new StringBuilder();
        sb.append("Order Information:\n");
        sb.append("Week day: ").append(order.getWeekDay()).append("\n");
        sb.append("Phone number: ")
                .append(order.getClient()
                        .getPhoneNumber()).append("\n");

        sb.append("Services:\n");
        for (Service service : services) {
            sb.append("- ").append(service.getTittle()).append("\n");
        }
        return sb.toString();
    }

    private BotApiMethod<?> chooseTime(CallbackQuery callbackQuery) {
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<String> timeList = new ArrayList<>(List.of("10:00", "12:30", "15:00", "17:30"));

        text.add("\uD83D\uDD19 Назад");
        cfg.add(1);
        data.add(order.name());
        var user = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        var order = orderRepo.findByClientAndRecord(user, false);
        List<Order> orders = orderRepo.findAllByRecordAndCompleted(true, false);
        String weekDay = order.getWeekDay();

        for (Order check : orders) {
            if (timeList.contains(check.getTimeOfRecord()) && weekDay.equals(check.getWeekDay())) {
                timeList.remove(check.getTimeOfRecord());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equals(order.getWeekDay())) {
            int minutes = now.getHour() * 60 + now.getMinute();
            int row = 0;
            for (String time : timeList) {
                if (Integer.parseInt(time.split(":")[0]) * 60 + Integer.parseInt(time.split(":")[1]) >= minutes) {
                    if (order.getTimeOfRecord() != null && order.getTimeOfRecord().equals(time)) {
                        text.add("✅ " + time);
                    } else {
                        text.add(time);
                    }
                    data.add(order_time_.name()+ time);
                    row += 1;
                }
            }
            if (row != 0) {
                cfg.add(row);
            }
        } else {
            for (String time : timeList) {
                if (order.getTimeOfRecord() != null && order.getTimeOfRecord().equals(time)) {
                    text.add("✅ " + time);
                } else {
                    text.add(time);
                }
                data.add(order_time_.name() + time);
            }
            cfg.add(timeList.size());
        }

        text.add("Далее \uD83D\uDD1C");
        cfg.add(1);
        data.add(order_service_.name());
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
        return chooseService(callbackQuery);
    }

    private BotApiMethod<?> putDay(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        var order = orderRepo.findByClientAndRecord(client, false);
        int dayNumber = Integer.parseInt(callbackQuery.getData().split("_")[2]);
        order.setWeekDay(DayOfWeek.of(dayNumber).getDisplayName(TextStyle.FULL, Locale.ROOT));
        orderRepo.save(order);
        return chooseTime(callbackQuery);
    }

    private BotApiMethod<?> chooseService(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        var order = orderRepo.findByClientAndRecord(client, false);
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
        for (Service service : services) {
            if (order.getServices().contains(service)) {
                text.add("✅" + service.getTittle());
            } else {
                text.add(service.getTittle());
            }
            data.add(order_service_.name() + service.getId());
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
        text.add("\uD83D\uDD19 Назад");
        text.add("Далее \uD83D\uDD1C");
        data.add(order_time_.name());
        data.add(order_next_step.name());
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
        for (int day = dayNumber; day <= 7; day++) {
            text.add(DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            data.add(order_day_.name() + day);
        }
        for (int day = 1; day < dayNumber; day++) {
            text.add(DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            data.add(order_day_.name() + day);
        }
        return keyboardFactory.getInlineKeyboard(
                text,
                List.of(3, 3, 1),
                data
        );
    }
}
