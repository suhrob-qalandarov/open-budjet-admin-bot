package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.Content;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MessageHandler implements Consumer<Message> {

    private final UserService userService;
    private final TelegramBot telegramBot;
    private final UserRepository userRepository;

    @Override
    public void accept(Message message) {
        String text = message.text();
        User dbUser = userService.getOrSaveTgUser(message.from());

        if (text != null && text.equals("/start")) {

            if (dbUser.getIsVoted() == null || !dbUser.getIsVoted()) {
                telegramBot.execute(new SendMessage(
                                dbUser.getId(),
                                "Botga xush kelibsiz!\nBu bot ochiq budjetning Xorazm viloyati Ko'shko'pir tumani uchun ishlab chiqilgan!" +
                                        "\n\nESLATMA!\nKontaktni ulashish tugmasi orqali telefon raqamingizni yuboring!"
                        ).replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton("Kontaktni ulashish").requestContact(true)))
                );
                return;
            } else {
                telegramBot.execute(new SendMessage(
                                dbUser.getId(),
                                "Botga xush kelibsiz!\nBu bot ochiq budjetning Xorazm viloyati Ko'shko'pir tumani uchun ishlab chiqilgan!" +
                                        "\n\n\nSiz ovoz bergansiz!"
                        )
                );
                return;
            }

        } else if (message.contact() != null) {
            String phone = message.contact().phoneNumber();
            if (!phone.startsWith("+")) phone = "+" + phone;
            dbUser.setPhoneNumber(phone);

            SendResponse response = telegramBot.execute(new SendMessage(
                    dbUser.getId(),
                    """
                            Telefon raqami tekshirilayapti, kuting!
                            
                            Хоразм вилояти Қўшкўпир тумани
                            Xosiyon mahallasi Afrosiyob ko'chasini asfalt qilish
                            """
            ).replyMarkup(new ReplyKeyboardRemove()));
            dbUser.setLastMessageId(response.message().messageId());

            userRepository.save(dbUser);

            boolean isVoted = userService.checkUserIsVoted(dbUser);
            List<String> phoneNumbers = userService.checkUserVoted(dbUser);

            if (isVoted) {
                telegramBot.execute(new EditMessageText(dbUser.getId(), dbUser.getLastMessageId(), "Siz ovoz bergansiz!"));
                dbUser.setIsVoted(true);
                userRepository.save(dbUser);
                return;

            } else {
                telegramBot.execute(new EditMessageText(dbUser.getId(), dbUser.getLastMessageId(), "Siz ovoz bermagansiz!" +
                        "\nIltimos ovoz bering va o'z hissangizni qo'shing!" +
                        "\n\nOvoz berish uchun Ovoz berish tugmasini bosing!"
                ).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton("Ovoz berish", "https://t.me/ochiqbudjetbot?start=052408466012"))));
                return;
            }

        } else telegramBot.execute(new SendMessage(
                dbUser.getId(),
                "Noto'g'ri buyruq!\nIltimos tugmalardan foydalaning!")
        );
    }
}
