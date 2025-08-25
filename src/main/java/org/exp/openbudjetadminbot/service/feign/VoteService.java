package org.exp.openbudjetadminbot.service.feign;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.transaction.Transactional;
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

    @Transactional
    public long fetchNewVotes(String uuid, Integer startPageNumber, Integer endPageNumber) {
        long oldVotes = voteRepository.count();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<Vote> newVotes = new ArrayList<>();

        try {
            for (int page = endPageNumber; page >= startPageNumber; page--) {
                System.out.println(page);
                VoteApiResponse response = apiFeignClient.getVotes(uuid, page);

                if (response.getContent() == null || response.getContent().isEmpty()) {
                    System.err.println("Response is empty! --- " + page);
                    continue;
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
            return -1;
        }

        voteRepository.saveAll(newVotes);
        return removeDuplicate(oldVotes);
    }

    @Transactional
    public long removeDuplicate(long oldVotesCount) {
        voteRepository.deleteDuplicatesByVoterPhoneLast6DigitAndVoteDate();
        long allVotesCount = voteRepository.count();
        return allVotesCount - oldVotesCount;
    }

    public void sendVotesByPhone(Long chatId, String query, int page, Integer messageId) {
        int size = 10;
        Page<Vote> votePage;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        if (query.length() > 6) {
            String last6Digits = query.substring(query.length() - 6);
            votePage = voteRepository.findAllByVoterPhoneLast6Digit(last6Digits, PageRequest.of(page, size));
        } else {
            votePage = voteRepository.findAllByVoterPhoneLast6DigitContaining(query, PageRequest.of(page, size));
        }

        if (votePage.isEmpty()) {
            telegramBot.execute(new SendMessage(chatId, "❌ Ma'lumot topilmadi."));
            return;
        }

        StringBuilder sb = new StringBuilder("📊 Ovozlar ro'yhati (varaq " + (page + 1) + ")\n\n");
        for (Vote v : votePage.getContent()) {
            sb.append("🆔: ").append(v.getId()).append("\n")
                    .append("📱 Telefon raqam: **-*").append(formatPhoneNumber(v.getVoterPhoneLast6Digit())).append("\n")
                    .append("📅 Sana: ").append(v.getVoteDate().format(dateFormatter)).append("\n")
                    .append("🕑 Soat: ").append(v.getVoteDate().format(timeFormatter))
                    .append("\n\n");
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (votePage.hasPrevious()) {
            buttons.add(new InlineKeyboardButton("⬅️ Oldingi - " + page).callbackData("phone_page_" + (page - 1) + "_" + query));
        }
        markup.addRow(new InlineKeyboardButton((page + 1) + "/" + votePage.getTotalPages()).callbackData("noop"));
        if (votePage.hasNext()) {
            buttons.add(new InlineKeyboardButton((page + 2) + " - Keyingi ➡️").callbackData("phone_page_" + (page + 1) + "_" + query));
        }

        if (!buttons.isEmpty()) {
            markup.addRow(buttons.toArray(new InlineKeyboardButton[0]));
        }

        if (messageId != null) {
            telegramBot.execute(new EditMessageText(chatId, messageId, sb.toString()).replyMarkup(markup));
        } else {
            telegramBot.execute(new SendMessage(chatId, sb.toString()).replyMarkup(markup));
        }
    }

    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 6) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 2) + "-" +
                phoneNumber.substring(2, 4) + "-" +
                phoneNumber.substring(4, 6);
    }

    public void sendVotesPage(Long chatId, int page, Integer messageId) {
        int size = 12; // har sahifada nechta Vote chiqadi
        Page<Vote> votePage = voteRepository.findAllByOrderByVoteDateDesc(PageRequest.of(page, size));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        if (votePage.isEmpty()) {
            telegramBot.execute(new SendMessage(chatId, "❌ Ma'lumot topilmadi."));
            return;
        }

        // Matn tayyorlash
        StringBuilder sb = new StringBuilder("📊 Barcha ovozlar ro'yhati (varaq " + (page + 1) + ")\n\n");
        for (Vote v : votePage.getContent()) {
            sb.append("🆔: ").append(v.getId()).append("\n")
                    .append("📱 Telefon raqam: **-*").append(formatPhoneNumber(v.getVoterPhoneLast6Digit())).append("\n")
                    .append("📅 Sana: ").append(v.getVoteDate().format(dateFormatter)).append("\n")
                    .append("🕑 Soat: ").append(v.getVoteDate().format(timeFormatter))
                    .append("\n\n");
        }

        // Inline tugmalar
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (votePage.hasPrevious()) {
            buttons.add(new InlineKeyboardButton("◀️️").callbackData("votes_page_0"));
            buttons.add(new InlineKeyboardButton("⬅️" + page).callbackData("votes_page_" + (page - 1)));
        }
        markup.addRow(new InlineKeyboardButton((page + 1) + "/" + votePage.getTotalPages()).callbackData("noop"));
        if (votePage.hasNext()) {
            buttons.add(new InlineKeyboardButton((page + 2) + "➡️").callbackData("votes_page_" + (page + 1)));
            buttons.add(new InlineKeyboardButton("▶️").callbackData("votes_page_" + (votePage.getTotalPages()-1)));
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