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
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackManager extends AbstractManager {

    final KeyboardFactory keyboardFactory;
    final AnswerMethodFactory methodFactory;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
//        return SendMessage.builder()
//                .chatId(message.getChatId())
//                .text("""
//                        🖐 Приветствую в Bardak Detailing!!!""")
//                .build();
        return sendPhoto(message, bot);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }

    public BotApiMethod<?> sendPhoto(Message message, Bot bot) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId());
        sendPhoto.setCaption("\uD83D\uDD90 Приветствую в Bardak Detailing!!!");
        sendPhoto.setPhoto(new InputFile("https://sun9-30.userapi.com/impg/JO1SbNlitAtudANRz-GXV6z3-X1MdlGKcJXulw/Y3dWAP6qeFY.jpg?size=483x604&quality=95&sign=1283e55592098e60caf779ed0f084f8c&c_uniq_tag=AhbORzBnHk1zCQ7cLw5qjuC5Zo1xNpdpiUP7lgcwJi8&type=album"));
        try {
            bot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
