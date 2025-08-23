package org.exp.openbudjetadminbot.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.service.feign.VoteService;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class CallbackQueryHandler implements Consumer<CallbackQuery> {

    private final TelegramBot telegramBot;
    private final VoteService voteService;

    @Override
    public void accept(CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        if (data.startsWith("votes_page_")) {
            int page = Integer.parseInt(data.replace("votes_page_", ""));

            voteService.sendVotesPage(
                    callbackQuery.from().id(),
                    page,
                    callbackQuery.message().messageId()
            );
        }
    }
}
