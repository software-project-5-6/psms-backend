package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    List<Message> findByConversationIdOrderByCreatedAt(UUID conversationId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversationId = :conversationId")
    int deleteByConversationId(UUID conversationId);

}
