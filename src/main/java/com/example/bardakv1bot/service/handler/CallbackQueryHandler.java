package com.example.bardakv1bot.service.handler;

import com.example.bardakv1bot.service.manager.start.FeedbackManager;
import com.example.bardakv1bot.service.manager.start.HelpManager;
import com.example.bardakv1bot.service.manager.start.OrderManager;
import com.example.bardakv1bot.service.manager.start.PhoneManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallbackQueryHandler {

    HelpManager helpManager;
    OrderManager orderManager;
    FeedbackManager feedbackManager;
    PhoneManager phoneManager;

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String[] words = callbackQuery.getData().split("_");
        switch (words[0]) {
            case "phone" -> {
                return phoneManager.answerCallbackQuery(callbackQuery, bot);
            }
            case "order" -> {
                return orderManager.answerCallbackQuery(callbackQuery, bot);
            }
            case "help" -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
            case "feedback" -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }
        }
        return null;
    }
}
