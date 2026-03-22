package com.majstro.psms.backend.rag;

import com.majstro.psms.backend.entity.Conversation;
import com.majstro.psms.backend.entity.Message;
import com.majstro.psms.backend.rag.dataModel.VectorDataBlock;
import com.majstro.psms.backend.rag.ingestion.IngestionService;
import com.majstro.psms.backend.rag.pipeline.QueryService;
import com.majstro.psms.backend.rag.util.RagUtil;
import com.majstro.psms.backend.repository.ConversationRepository;
import com.majstro.psms.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RagServices {

    private final IngestionService ingestionService;
    private final QueryService queryService;
    private final RagUtil ragUtil;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;


    public void embbedAndStoreDocument(
            MultipartFile file,
            String uploadedBy,
            String tags,
            String projectId) {


        VectorDataBlock vectorBlock = null;
        try {
            vectorBlock = ragUtil.convertDocumentToVectorDataBlock(file, uploadedBy, tags, projectId);
            ingestionService.indexRagDocument(vectorBlock);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void embbedAndStoreChat(
            String message,
            String role,
            String userId,
            String conversationId) {

        VectorDataBlock vectorBlock = null;

        try {
            String projectId = conversationRepository.findById(UUID.fromString(conversationId)).get().getProjectId();

            createNewMessage(conversationId, message, role);

            vectorBlock = ragUtil.convertChatToVectorDataBlock(message, role, userId, projectId, conversationId);

            ingestionService.indexRagDocument(vectorBlock);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public String createConversation(String projectId, String title, String userId) {

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setProjectId(projectId);
        var response = conversationRepository.save(conversation);

        return response.getId().toString();
    }


    private void createNewMessage(String conversationId, String message, String role) {
        Message msg = new Message();
        msg.setConversationId(UUID.fromString(conversationId));
        msg.setContent(message);
        msg.setRole(role);
        messageRepository.save(msg);
    }

    public List<Message> getConversationMessages(String conversationId) {

        return messageRepository.findByConversationIdOrderByCreatedAt(UUID.fromString(conversationId));
    }

    public List<Conversation> getProjectConversations(String projectId) {
        return conversationRepository.findByProjectIdOrderByCreatedAt(projectId);
    }

    public String query(String userQuery, String projectId, String conversationId) {
        return queryService.answerUserQuery(userQuery, projectId, conversationId);

    }

    public void deleteDocs(String projectId) {
        ingestionService.deleteProjectDocs(projectId);
    }


}
