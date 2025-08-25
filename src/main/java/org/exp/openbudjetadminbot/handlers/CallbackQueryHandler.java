package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.repository.UserRepository;
import org.exp.openbudjetadminbot.service.face.UserService;
import org.exp.openbudjetadminbot.service.feign.VoteService;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackQueryHandler implements Consumer<CallbackQuery> {

    private final TelegramBot telegramBot;
    private final VoteService voteService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void accept(CallbackQuery callbackQuery) {
        String data = callbackQuery.data();

        if (data.equals("noop")) return;

        User dbUser = userService.getOrSaveTgUser(callbackQuery.from());

        if (dbUser.getLastMessageId() != null) {
            try {
                telegramBot.execute(new DeleteMessage(dbUser.getId(), dbUser.getLastMessageId()));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Message o'chirishda xatolik: userId{}, messageId{}", dbUser.getId(), dbUser.getLastMessageId());
            }
        }

        if (data.equals("voted")) {
            telegramBot.execute(new EditMessageText(dbUser.getId(), callbackQuery.message().messageId(), "😊Ovoz berganingizdan xursandmiz!"));
            dbUser.setIsVoted(true);
            userRepository.save(dbUser);

            telegramBot.execute(new SendMessage(
                    dbUser.getId(),
                    "👇Quyidagilardan birini tanlang:"
            ).replyMarkup(new ReplyKeyboardMarkup(
                    new KeyboardButton("📊Ovozlarni ko'rish"),
                    new KeyboardButton("✅Raqamni tekshirish")
            ).resizeKeyboard(true)));
            return;

        } else if (data.startsWith("votes_page_")) {
            int page = Integer.parseInt(data.replace("votes_page_", ""));

            voteService.sendVotesPage(
                    callbackQuery.from().id(),
                    page,
                    callbackQuery.message().messageId()
            );
            return;

        } else if (data.startsWith("phone_page_")) {
            String[] parts = data.split("_");
            int page = Integer.parseInt(parts[2]);
            String query = parts[3];

            voteService.sendVotesByPhone(
                    callbackQuery.from().id(),
                    query,
                    page,
                    callbackQuery.message().messageId()
            );
            return;
        }
    }
}
