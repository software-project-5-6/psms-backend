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
import org.stringtemplate.v4.ST;

import java.io.IOException;
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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ingestionService.indexRagDocument(vectorBlock);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public void embbedAndStoreChat(
            String message,
            String role,
            String userId,
            String conversationId
    ) {


        VectorDataBlock vectorBlock = null;
        String projectId = conversationRepository.findById(UUID.fromString(conversationId)).get().getProjectId();

        createNewMessage(conversationId, message, role);

        try {
            vectorBlock = ragUtil.convertChatToVectorDataBlock(message, role, userId, projectId, conversationId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            ingestionService.indexRagDocument(vectorBlock);
        } catch (RuntimeException e) {
            throw e;
        }

    }

    public String createConversation(String projectId, String title, String userId) {

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        var response = conversationRepository.save(conversation);
        var newConversationId = conversation.getId().toString();

        return newConversationId;
    }


    private void createNewMessage(String conversationId, String message, String role) {
        Message msg = new Message();
        msg.setConversationId(UUID.fromString(conversationId));
        msg.setContent(message);
        msg.setRole(role);
        messageRepository.save(msg);
    }

    public String query(String userQuery, String projectId) {
        return queryService.answerUserQuery(userQuery, projectId);

    }

    public void deleteDocs(String projectId) {
        ingestionService.deleteProjectDocs(projectId);
    }


}
