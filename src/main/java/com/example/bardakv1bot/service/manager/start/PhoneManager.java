package com.example.bardakv1bot.service.manager.start;

import com.example.bardakv1bot.entity.Client;
import com.example.bardakv1bot.entity.PhoneNumber;
import com.example.bardakv1bot.factory.AnswerMethodFactory;
import com.example.bardakv1bot.factory.KeyboardFactory;
import com.example.bardakv1bot.repository.ClientRepo;
import com.example.bardakv1bot.repository.PhoneRepo;
import com.example.bardakv1bot.service.manager.AbstractManager;
import com.example.bardakv1bot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.example.bardakv1bot.data.Callback.*;

/**
 * @author nerzon
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PhoneManager extends AbstractManager {
    PhoneRepo phoneRepo;
    ClientRepo clientRepo;
    AnswerMethodFactory methodFactory;
    KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String[] words = callbackQuery.getData().split("_");
        switch (words.length) {
            case 1 -> {
                return mainMenu(callbackQuery, null, null);
            }
            case 2 -> {
                switch (words[1]) {
                    case "backspace" -> {
                        return removeDigit(callbackQuery);
                    }
                    case "drop" -> {
                        return dropNumber(callbackQuery);
                    }
                }
            }
            case 3 -> {
                return addDigit(callbackQuery, words[2]);
            }
        }
        return null;
    }

    private BotApiMethod<?> addDigit(CallbackQuery callbackQuery, String digit) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        PhoneNumber phoneNumber = phoneRepo.findByClient(client);
        phoneNumber.plusDigit(digit);
        phoneRepo.save(phoneNumber);
        return mainMenu(callbackQuery, client, phoneNumber);
    }

    private BotApiMethod<?> removeDigit(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        PhoneNumber phoneNumber = phoneRepo.findByClient(client);
        phoneNumber.minusDigit();
        phoneRepo.save(phoneNumber);
        return mainMenu(callbackQuery, client, phoneNumber);
    }

    public BotApiMethod<?> mainMenu(CallbackQuery callbackQuery, Client client, PhoneNumber phoneNumber) {
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите номер телефона, используя кнопки на циферблате",
                getMainKeyboard(callbackQuery.getMessage().getChatId(), client, phoneNumber)
        );
    }

    public BotApiMethod<?> dropNumber(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        PhoneNumber phoneNumber = phoneRepo.findByClient(client);
        phoneNumber.drop();
        phoneRepo.save(phoneNumber);
        return mainMenu(callbackQuery, client, phoneNumber);
    }

    private InlineKeyboardMarkup getMainKeyboard(Long chatId, Client client, PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            if (client == null) {
                client = clientRepo.findById(chatId).orElseThrow();
            }
            phoneNumber = phoneRepo.findByClient(client);
            if (phoneNumber == null) {
                phoneNumber = new PhoneNumber();
                phoneNumber.setClient(client);
                phoneRepo.save(phoneNumber);
            }
        }
        return keyboardFactory.getInlineKeyboard(
                List.of(
                        phoneNumber.getPhoneNumber(),
                        "1\uFE0F⃣", "2\uFE0F⃣", "3\uFE0F⃣",
                        "4\uFE0F⃣", "5\uFE0F⃣", "6\uFE0F⃣",
                        "7\uFE0F⃣", "8\uFE0F⃣", "9\uFE0F⃣",
                        "Назад", "0\uFE0F⃣", "Стереть",
                        "Ввод", "Сброс"
                ),
                List.of(
                        1, 3, 3, 3, 3, 2
                ),
                List.of(
                        "blank",
                        phone_digit_ + "1", phone_digit_ + "2", phone_digit_ + "3",
                        phone_digit_ + "4", phone_digit_ + "5", phone_digit_ + "6",
                        phone_digit_ + "7", phone_digit_ + "8", phone_digit_ + "9",
                        "next_step", phone_digit_ + "0", phone_backspace.name(),
                        order_enter.name(), phone_drop.name()
                )
        );
    }
}


