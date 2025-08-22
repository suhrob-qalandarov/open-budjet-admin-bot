package org.exp.openbudjetadminbot.service.face;

import org.exp.openbudjetadminbot.models.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    User getOrSaveTgUser(com.pengrad.telegrambot.model.User user);

    boolean checkUserIsVoted(User dbUser);

    List<String> checkUserVoted(String phoneNumber);
}
