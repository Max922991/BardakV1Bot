package com.example.bardakv1bot.service.handler;

import com.example.bardakv1bot.service.manager.start.*;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.example.bardakv1bot.data.CallbackData.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallbackQueryHandler {

    StartManager startManager;
    HelpManager helpManager;
    OrderManager orderManager;
    FeedbackManager feedbackManager;
    PhoneManager phoneManager;

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String keyWord = callbackData.split("_")[0];
        if ("close".equals(keyWord)) {
            orderManager.answerCallbackQuery(callbackQuery, bot);
        }
        if ("phone".equals(keyWord)) {
            if ("phone_enter".equals(callbackData)) {
                return orderManager.answerCallbackQuery(callbackQuery, bot);
            }
            return phoneManager.answerCallbackQuery(callbackQuery, bot);
        }
        if ("finish_order".equals(callbackData)) {
                return orderManager.answerCallbackQuery(callbackQuery, bot);
        }
        if ("next_step".equals(callbackData)) {
            return orderManager.answerCallbackQuery(callbackQuery, bot);
        }
        if ("service".equals(keyWord)) {
            return orderManager.answerCallbackQuery(callbackQuery, bot);
        }
        if ("time".equals(keyWord)) {
            if (callbackData.split("_").length == 2) {
                return orderManager.answerCallbackQuery(callbackQuery, bot);
            }
            return orderManager.answerCallbackQuery(callbackQuery, bot);
        }
        switch (callbackData) {
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }
            case CARWASH, DAY_1, DAY_2, DAY_3, DAY_4, DAY_5, DAY_6, DAY_7 -> {
                return orderManager.answerCallbackQuery(callbackQuery, bot);
            }
        }
        return null;
    }
}
