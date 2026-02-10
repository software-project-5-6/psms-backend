package com.majstro.psms.backend.rag;


import com.majstro.psms.backend.rag.dataModel.RAGDocument;
import com.majstro.psms.backend.rag.ingestion.IngestionService;
import com.majstro.psms.backend.rag.pipeline.QueryService;
import com.majstro.psms.backend.rag.util.RagUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RagServices {

   private final IngestionService ingestionService;
   private final QueryService queryService;
   private final RagUtil ragUtil;


   public void embbedAndStore(
           MultipartFile file,
           String uploadedBy,
           String tags,
           String projectId) {

       RAGDocument ragDocument = null;
       try {
           ragDocument = ragUtil.convertToRagDocument(file,uploadedBy,tags,projectId);
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
       ingestionService.indexRagDocument(ragDocument);

   }

   public String query(String userQuery,String projectId){

       return queryService.answerUserQuery(userQuery,projectId);
   }

   public void deleteDocs(String projectId){
       ingestionService.deleteProjectDocs(projectId);
   }


}
