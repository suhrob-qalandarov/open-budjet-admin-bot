package org.exp.openbudjetadminbot.service.feign;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.exp.openbudjetadminbot.models.Vote;
import org.exp.openbudjetadminbot.models.dto.response.VoteApiResponse;
import org.exp.openbudjetadminbot.repository.VoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final ApiFeignClient apiFeignClient;
    private final VoteRepository voteRepository;
    private final TelegramBot telegramBot;

    public void fetchNewVotes(String uuid, Integer startPageNumber, Integer endPageNumber) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<Vote> newVotes = new ArrayList<>();

        try {
            // Teskari tartibda sahifalarni aylanish
            for (int page = endPageNumber; page >= startPageNumber; page--) {
                System.out.println(page);
                VoteApiResponse response = apiFeignClient.getVotes(uuid, page);

                if (response.getContent() == null || response.getContent().isEmpty()) {
                    System.err.println("Response is empty! --- " + page);
                    continue; // Bo'sh javob bo'lsa, keyingi sahifaga o'tish
                }

                for (VoteApiResponse.VoteContent item : response.getContent()) {
                    LocalDateTime voteDate = LocalDateTime.parse(item.getVoteDate(), formatter);

                    String cleanedPhone = item.getPhoneNumber().replaceAll("[^0-9]", "");
                    /*String last6Digits = cleanedPhone.length() >= 6
                            ? cleanedPhone.substring(cleanedPhone.length() - 6)
                            : cleanedPhone;*/

                    Vote vote = Vote.builder()
                            .voterPhoneLast6Digit(cleanedPhone)
                            .voteDate(voteDate)
                            .build();

                    newVotes.add(vote);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(newVotes);

        voteRepository.saveAll(newVotes);
    }

    public void sendVotesPage(Long chatId, int page, Integer messageId) {
        int size = 10; // har sahifada nechta Vote chiqadi
        Page<Vote> votePage = voteRepository.findAll(PageRequest.of(page, size));

        if (votePage.isEmpty()) {
            telegramBot.execute(new SendMessage(chatId, "❌ Ma'lumot topilmadi."));
            return;
        }

        // Matn tayyorlash
        StringBuilder sb = new StringBuilder("📊 Ovozlar ro'yhati (page " + (page + 1) + ")\n\n");
        for (Vote v : votePage.getContent()) {
            sb.append("🆔: ").append(v.getId()).append("\n")
                    .append("📅Sana: ").append(v.getVoteDate()).append("\n")
                    .append("📱Ovoz bergan telfon: **-*").append(v.getVoterPhoneLast6Digit())
                    //.append(v.getVotedUserPhoneNumbers() == null ? "-" : String.join(", ", v.getVotedUserPhoneNumbers()))
                    .append("\n\n");
        }

        // Inline tugmalar
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (votePage.hasPrevious()) {
            buttons.add(new InlineKeyboardButton("⬅️ Oldingi")
                    .callbackData("votes_page_" + (page - 1)));
        }
        if (votePage.hasNext()) {
            buttons.add(new InlineKeyboardButton("Keyingi ➡️")
                    .callbackData("votes_page_" + (page + 1)));
        }
        if (!buttons.isEmpty()) {
            markup.addRow(buttons.toArray(new InlineKeyboardButton[0]));
        }

        // Agar callback’dan kelgan bo‘lsa eski xabarni yangilash
        if (messageId != null) {
            telegramBot.execute(new EditMessageText(chatId, messageId, sb.toString())
                    .replyMarkup(markup));
        } else {
            // Reply button bosilganda birinchi sahifani yuborish
            telegramBot.execute(new SendMessage(chatId, sb.toString())
                    .replyMarkup(markup));
        }
    }
}