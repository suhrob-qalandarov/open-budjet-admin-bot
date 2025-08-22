package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    @SneakyThrows
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

            telegramBot.execute(new SendMessage(
                    dbUser.getId(),
                    """
                            Хоразм вилояти Қўшкўпир тумани
                            Xosiyon mahallasi Afrosiyob ko'chasini asfalt qilish"""
            ).replyMarkup(new ReplyKeyboardRemove()));

            SendResponse response = telegramBot.execute(new SendMessage(dbUser.getId(), """
                    Telefon raqami tekshirilayapti, kuting!
                    Ovoz berilganligi aniqligi telefon raqamining oxirgi 6ta raqami orqali tekshiriladi!"""
            ));

            Thread.sleep(2500);

            System.out.println(response.message().messageId());
            dbUser.setLastMessageId(response.message().messageId());
            userRepository.save(dbUser);

            List<String> phoneNumbers = userService.checkUserVoted(dbUser.getPhoneNumber());

            System.out.println(phoneNumbers);

            if (phoneNumbers.size()==1) {
                telegramBot.execute(new EditMessageText(dbUser.getId(), response.message().messageId(), "Siz ovoz bergansiz!"));
                dbUser.setIsVoted(true);
                userRepository.save(dbUser);
                return;

            } else if (phoneNumbers.size() > 1) {
                telegramBot.execute(new EditMessageText(dbUser.getId(), response.message().messageId(), "Sizning telefon raqamingizning oxirgi 6 ta raqami(" + phoneNumbers.getFirst() + ")ga o'xshash "
                        + phoneNumbers.size() + " ta raqam topildi!"
                ));
                telegramBot.execute(new SendMessage(
                        dbUser.getId(),
                        "Yana bir bor ovoz berib sinab ko'ring buning uchun Ovoz berish tugmasini bosing!"
                        ).replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton("Ovoz berish").url("https://t.me/ochiqbudjetbot?start=052408466012")
                        ))
                );
                return;

            } else {
                BaseResponse response1 = telegramBot.execute(new EditMessageText(dbUser.getId(), dbUser.getLastMessageId(), "Siz ovoz bermagansiz!"));
                System.out.println(response1);
                telegramBot.execute(new SendMessage(
                        dbUser.getId(),
                        """
                                Iltimos ovoz bering va o'z hissangizni qo'shing!
                                Ovoz berish uchun Ovoz berish tugmasini bosing!"""
                        ).replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton("Ovoz berish").url("https://t.me/ochiqbudjetbot?start=052408466012")
                        ))
                );
                return;
            }

        } else telegramBot.execute(new SendMessage(
                dbUser.getId(),
                "Noto'g'ri buyruq!\nIltimos tugmalardan foydalaning!")
        );
    }
}
