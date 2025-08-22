package org.exp.openbudjetadminbot.repository;

import org.exp.openbudjetadminbot.models.FetchState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FetchStateRepository extends JpaRepository<FetchState, String> {
}