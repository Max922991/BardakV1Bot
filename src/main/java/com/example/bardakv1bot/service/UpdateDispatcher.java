package com.example.bardakv1bot.service;

import com.example.bardakv1bot.entity.Action;
import com.example.bardakv1bot.entity.Client;
import com.example.bardakv1bot.repository.ClientRepo;
import com.example.bardakv1bot.service.handler.CallbackQueryHandler;
import com.example.bardakv1bot.service.handler.CommandHandler;
import com.example.bardakv1bot.service.handler.MessageHandler;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UpdateDispatcher {
    final CallbackQueryHandler callbackQueryHandler;
    final CommandHandler commandHandler;
    final MessageHandler messageHandler;
    final ClientRepo clientRepo;

    public BotApiMethod<?> distribute(Update update, Bot bot) {
        if (update.hasCallbackQuery()) {
            checkUser(update.getCallbackQuery().getMessage().getChatId());
            return callbackQueryHandler.answer(update.getCallbackQuery(), bot);
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            checkUser(message.getChatId());
            if (message.hasText()) {
                if (message.getText().startsWith("/")) {
                    return commandHandler.answer(message, bot);
                }
            }
            return messageHandler.answer(message, bot);
        }
        log.info("Unsupported update: " + update);
        return null;
    }

    private void checkUser(Long chatId) {
        var user = clientRepo.findById(chatId).orElse(null);
        if (user == null) {
            Client client = Client.builder()
                    .id(chatId)
                    .action(Action.FREE)
                    .build();
            clientRepo.save(client);
        }
    }
}
