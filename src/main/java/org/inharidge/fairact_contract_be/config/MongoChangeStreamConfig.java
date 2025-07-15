package org.inharidge.fairact_contract_be.config;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.entity.Contract;
import org.inharidge.fairact_contract_be.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.messaging.*;

@Configuration
public class MongoChangeStreamConfig {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SseEmitterManager sseEmitterManager;
    @Autowired
    private MinioService minioService;

    @PostConstruct
    public void watchClauseFieldUpdates() {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer(mongoTemplate);

        ChangeStreamOptions options = ChangeStreamOptions.builder()
                .returnFullDocumentOnUpdate()
                .build();

        ChangeStreamRequest.ChangeStreamRequestOptions requestOptions =
                new ChangeStreamRequest.ChangeStreamRequestOptions("fairact", "contract", options);

        ChangeStreamRequest<Document> request = new ChangeStreamRequest<>(
                (Message<ChangeStreamDocument<Document>, Document> message) -> {
                    ChangeStreamDocument<Document> raw = message.getRaw();

                    if (raw.getOperationType() == OperationType.UPDATE
                            && raw.getUpdateDescription() != null
                            && raw.getUpdateDescription().getUpdatedFields().containsKey("clauses")) {

                        Document fullDoc = raw.getFullDocument();
                        if (fullDoc != null) {
                            Contract contract = convertDocumentToContract(fullDoc);
                            ContractDetailDTO dto = contract.toContractDetailDTO();

                            if (dto.getFile_uri() != null) {
                                String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getFile_uri());
                                dto.setFile_uri(preSignedUrl);
                            }
                            if (dto.getWorker_sign_url() != null) {
                                String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getWorker_sign_url());
                                dto.setWorker_sign_url(preSignedUrl);
                            }
                            if (dto.getOwner_sign_url() != null) {
                                String preSignedUrl = minioService.getPreSignedUrlByBucketUrl(dto.getOwner_sign_url());
                                dto.setOwner_sign_url(preSignedUrl);
                            }

                            System.out.println("🟡 감지된 Contract.clauses 변경: " + contract.getId());

                            if (contract.getWorker_id() != null)
                                sseEmitterManager.sendToUser(contract.getWorker_id(), "toxic-clause", dto);
                            if (contract.getOwner_id() != null)
                                sseEmitterManager.sendToUser(contract.getOwner_id(), "toxic-clause", dto);
                        }
                    }
                },
                requestOptions
        );

        container.register(request, Document.class);
        container.start();
    }


    private Contract convertDocumentToContract(Document doc) {
        MongoConverter converter = mongoTemplate.getConverter();
        return converter.read(Contract.class, doc);
    }
}