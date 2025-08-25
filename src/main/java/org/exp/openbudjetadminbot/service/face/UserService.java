package org.exp.openbudjetadminbot.service.face;

import org.exp.openbudjetadminbot.models.User;
import org.exp.openbudjetadminbot.models.Vote;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    User getOrSaveTgUser(com.pengrad.telegrambot.model.User user);

    User updateDbUser(User user);

    List<Vote> checkUserVoted(String phoneNumber);

    void sendBaseUpdateMessageToUsers(long newVotesCount);
}
