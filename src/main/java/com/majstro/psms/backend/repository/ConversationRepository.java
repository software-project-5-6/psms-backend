package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Conversation;
import com.majstro.psms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

}
