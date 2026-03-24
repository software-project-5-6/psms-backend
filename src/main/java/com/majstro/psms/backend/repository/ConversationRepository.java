package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByProjectIdOrderByCreatedAt(String projectId);

    void deleteById(UUID id);
}
