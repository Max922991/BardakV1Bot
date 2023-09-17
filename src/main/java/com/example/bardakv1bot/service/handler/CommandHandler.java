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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.example.bardakv1bot.data.Command.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandHandler {

    StartManager startManager;
    HelpManager helpManager;
    FeedbackManager feedbackManager;
    OrderManager orderManager;

    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START -> {
                return startManager.answerCommand(message, bot);
            }
            case FEEDBACK -> {
                return feedbackManager.answerCommand(message, bot);
            }
            case HELP -> {
                return helpManager.answerCommand(message, bot);
            }
            case CARWASH -> {
                return orderManager.answerCommand(message, bot);
            }
            default -> {
                return defaultAnswer(message);
            }
        }
    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Unsupported command!!!")
                .build();
    }

    private BotApiMethod<?> help(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        🌟 Доступные команды: 
                                                
                        - start
                        - help
                        - feedback
                                                
                        🌟 Доступные функции:
                                                
                        - Запись на услугу
                        - Получение информации об услуге
                                                
                        """)
                .build();
    }

    private BotApiMethod<?> feedback(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                        🌟 Ссылки для связи с нами!!!
                                                
                        ✅Telegram - https://t.me/bardak_detailing
                        ✅Telephone - +79881234567""")
                .disableWebPagePreview(true)
                .build();
    }
}
