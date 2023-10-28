package com.example.bardakv1bot.service.handler;

import com.example.bardakv1bot.entity.Client;
import com.example.bardakv1bot.repository.ClientRepo;
import com.example.bardakv1bot.service.manager.start.OrderManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageHandler {
    private final ClientRepo clientRepo;
    private final OrderManager orderManager;
    public BotApiMethod<?> answer(Message message, Bot bot) {
        Long id = message.getChatId();
        Client client = clientRepo.findById(id).orElseThrow();
        switch (client.getAction()) {
            case FREE -> {
                return null;
            }
        }
        return null;
    }
}
