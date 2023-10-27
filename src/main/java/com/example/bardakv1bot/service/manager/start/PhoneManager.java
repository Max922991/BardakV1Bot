package com.example.bardakv1bot.service.manager.start;

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

import static com.example.bardakv1bot.data.CallbackData.*;

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
        String data = callbackQuery.getData();
        String[] words = data.split("_");
        if (phone_drop.equals(data)) {
            return dropNumber(callbackQuery);
        }
        switch (words.length) {
            case 1 -> {
                return mainMenu(callbackQuery);
            }
            case 2 -> {
                return removeDigit(callbackQuery);
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
        return mainMenu(callbackQuery);
    }

    private BotApiMethod<?> removeDigit(CallbackQuery callbackQuery) {
        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        PhoneNumber phoneNumber = phoneRepo.findByClient(client);
        phoneNumber.minusDigit();
        phoneRepo.save(phoneNumber);
        return mainMenu(callbackQuery);
    }

    public BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите номер телефона, используя кнопки на циферблате",
                getMainKeyboard(callbackQuery.getMessage().getChatId())
        );
    }

    public BotApiMethod<?> dropNumber(CallbackQuery callbackQuery) {

        var client = clientRepo.findById(callbackQuery.getMessage().getChatId()).orElseThrow();
        PhoneNumber phoneNumber = phoneRepo.findByClient(client);
        phoneNumber.drop();
        phoneRepo.save(phoneNumber);

        return mainMenu(callbackQuery);
    }

    private InlineKeyboardMarkup getMainKeyboard(Long chatId) {
        var client = clientRepo.findById(chatId).orElseThrow();
        PhoneNumber phoneNumber = phoneRepo.findByClient(client);
        if (phoneNumber == null) {
            phoneNumber = new PhoneNumber();
            phoneNumber.setClient(client);
            phoneRepo.save(phoneNumber);
        }
        return keyboardFactory.getInlineKeyboard(
                List.of(
                        phoneNumber.getPhoneNumber(),
                        "1", "2", "3",
                        "4", "5", "6",
                        "7", "8", "9",
                        "Назад", "0", "Стереть",
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
                        "next_step", phone_digit_ + "0", phone_backspace,
                        phone_enter, phone_drop
                )
        );
    }
}


