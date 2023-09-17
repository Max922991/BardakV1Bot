package com.example.bardakv1bot.service.manager.start;

import com.example.bardakv1bot.factory.AnswerMethodFactory;
import com.example.bardakv1bot.factory.KeyboardFactory;
import com.example.bardakv1bot.service.manager.AbstractManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackManager extends AbstractManager {

    final KeyboardFactory keyboardFactory;
    final AnswerMethodFactory methodFactory;
    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        🖐 Приветствую в Bardak Detailing!!!""")
                .build();
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }
}
