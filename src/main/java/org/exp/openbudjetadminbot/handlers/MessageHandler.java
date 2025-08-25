package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.models.Vote;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.exp.openbudjetadminbot.service.feign.VoteService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandler implements Consumer<Message> {

    private final TelegramBot telegramBot;
    private final UserService userService;
    private final VoteService voteService;

    @Transactional
    @SneakyThrows
    @Override
    public void accept(Message message) {
        String text = message.text();
        User dbUser = userService.getOrSaveTgUser(message.from());

        if (message.contact() != null) {
            String phone = message.contact().phoneNumber();
            if (!phone.startsWith("+")) phone = "+" + phone;
            dbUser.setPhoneNumber(phone);
            userService.updateDbUser(dbUser);

            telegramBot.execute(new SendMessage(
                    dbUser.getId(),
                    """
                            📥Ovoz berilganligi aniqligi telefon raqamining oxirgi 6ta raqami orqali tekshiriladi!"""
            ).replyMarkup(new ReplyKeyboardRemove()));

            SendResponse response = telegramBot.execute(new SendMessage(dbUser.getId(), """
                        📲Telefon raqami tekshirilayapti, kuting!"""
            ));

            List<Vote> votes = userService.checkUserVoted(dbUser.getPhoneNumber());
            if (response.isOk()) {
                Thread.sleep(2000);
            }

            if (votes.size() == 1) {
                telegramBot.execute(new EditMessageText(dbUser.getId(), response.message().messageId(), "✅Siz ovoz bergansiz!"));
                dbUser.setIsVoted(true);
                userService.updateDbUser(dbUser);

                telegramBot.execute(new SendMessage(
                        dbUser.getId(),
                        "👇Quyidagilardan birini tanlang:"
                ).replyMarkup(new ReplyKeyboardMarkup(
                        new KeyboardButton("📊Ovozlarni ko'rish"),
                        new KeyboardButton("✅Raqamni tekshirish")
                ).resizeKeyboard(true)));
                return;

            } else if (votes.size() > 1) {

                StringBuilder similarPhoneNumbers = new StringBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (int i = 0; i < votes.size(); i++) {
                    Vote vote = votes.get(i);
                    similarPhoneNumbers.append(i + 1)
                            .append(". 📱: **-*")
                            .append(voteService.formatPhoneNumber(vote.getVoterPhoneLast6Digit()))
                            .append("\n🕗")
                            .append(vote.getVoteDate().format(formatter))
                            .append("\n\n");
                }

                telegramBot.execute(new EditMessageText(
                        dbUser.getId(),
                        response.message().messageId(),
                        "⁉\uFE0FOvoz berilganligi no'malum⁉\n\uD83D\uDCF2Telefon raqami: **-*"
                                + voteService.formatPhoneNumber(votes.getFirst().getVoterPhoneLast6Digit()) + " ga mos "
                                + votes.size() + " ta raqam topildi:\n\n" +
                                similarPhoneNumbers + """
                                👆Ro'yhatda ovoz bergan vaqtiga mos bo'lsa Ovoz berdim
                                📲Aks holda Ovoz berish tugmasini bosing"""
                        ).replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton("📥Ovoz berish📥").url("https://t.me/ochiqbudjetbot?start=052408466012"),
                                new InlineKeyboardButton("📨Ovoz berdim📨").callbackData("voted")
                        ))
                );
                return;

            } else {
                SendResponse execute = (SendResponse) telegramBot.execute(new EditMessageText(dbUser.getId(), response.message().messageId(), "❌️Siz ovoz bermagansiz!"));
                dbUser.setLastMessageId(execute.message().messageId());
                userService.updateDbUser(dbUser);
                telegramBot.execute(new SendMessage(
                                dbUser.getId(),
                                """
                                        🚀️️️️️️Iltimos ovoz bering va o'z hissangizni qo'shing!
                                        📣️️️️️️Ovoz berish uchun Ovoz berish tugmasini bosing!
                                        🗳Ovoz berib Ovoz berdim tugmasini bosing!"""
                        ).replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton("📥Ovoz berish📥").url("https://t.me/ochiqbudjetbot?start=052408466012"),
                                new InlineKeyboardButton("📨Ovoz berdim📨").callbackData("voted")
                        ))
                );
                return;
            }

        }

        if (text != null){
            if (text.equals("/start")) {

                telegramBot.execute(new SendMessage(
                                dbUser.getId(),
                                "🤗Botga xush kelibsiz!\n\uD83E\uDD16Bu bot Xorazm vil. Qo'shko'pir tumani, Xosiyon qishlog'i uchun ishlab chiqilgan!" +
                                        "\n\n❗ESLATMA\n📲Kontaktni ulashish tugmasi orqali telefon raqamingizni yuboring!"
                        ).replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton("📲Kontaktni ulashish").requestContact(true)).resizeKeyboard(true))
                );
                return;

            }  else if (Objects.requireNonNull(text).equals("📊Ovozlarni ko'rish")) {
                voteService.sendVotesPage(message.chat().id(), 0, null);

            } else if (text.equals("✅Raqamni tekshirish")) {
                telegramBot.execute(new SendMessage(dbUser.getId(),
                        "📞Telefon raqamini yozib yuboring!\n📂Masalan: +998901234567, 901234567, 4567, 45, 67"
                ));

            } else if (text.startsWith("68ab")) {
                long newVotesCount = 0;
                String uuid = text.substring(text.lastIndexOf("_") + 1);

                newVotesCount = voteService.fetchNewVotes(uuid, 0, 10);

                if (newVotesCount == -1) {
                    telegramBot.execute(new SendMessage(dbUser.getId(),
                            "💢Ma'lumotlarni o'qishda xatolik!"
                    ));
                    return;
                }

                telegramBot.execute(new SendMessage(dbUser.getId(),
                        """
                                🆕Yangi ma'lumotlar o'qib olindi va saqlandi!
                                💢Dublikat ma'lumotlar o'chirildi!"""
                ));
                userService.sendBaseUpdateMessageToUsers(newVotesCount);

            } else if (text.matches("\\d+")) {
                voteService.sendVotesByPhone(dbUser.getId(), text, 0, null);

            } else {
                telegramBot.execute(new SendMessage(
                        dbUser.getId(),
                        "Siz kiritdingiz: " + text
                ));
            }
        }
    }
}
