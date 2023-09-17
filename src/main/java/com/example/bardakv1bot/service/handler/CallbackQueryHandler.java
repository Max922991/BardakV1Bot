package com.example.bardakv1bot.service.handler;

import com.example.bardakv1bot.service.manager.start.FeedbackManager;
import com.example.bardakv1bot.service.manager.start.HelpManager;
import com.example.bardakv1bot.service.manager.start.OrderManager;
import com.example.bardakv1bot.service.manager.start.StartManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.example.bardakv1bot.data.CallbackData.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallbackQueryHandler {

    StartManager startManager;
    HelpManager helpManager;
    OrderManager orderManager;
    FeedbackManager feedbackManager;

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String keyWord = callbackData.split("_")[0];
        switch (callbackData) {
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }
            case CARWASH, DAY_1, DAY_2, DAY_3, DAY_4, DAY_5, DAY_6, DAY_7, WASH1, WASH2, WASH3, WASH4 -> {
                return orderManager.answerCallbackQuery(callbackQuery, bot);
            }
        }
        return null;
    }
}
