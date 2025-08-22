package org.exp.openbudjetadminbot.repository;

import org.exp.openbudjetadminbot.models.Content;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ContentRepository extends Repository<Content, UUID> {
    Content save(Content content);

    int count();
}