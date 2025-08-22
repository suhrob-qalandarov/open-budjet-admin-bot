package org.exp.openbudjetadminbot.repository

import org.exp.openbudjetadminbot.models.User
import org.springframework.data.repository.Repository

interface UserRepository : Repository<User, Long> {
}