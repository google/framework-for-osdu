package com.osdu.repository.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.osdu.exception.IngestException;
import com.osdu.mapper.SchemaDataMapper;
import com.osdu.model.dto.SchemaDataDto;
import com.osdu.repository.SchemaDataRepository;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class GcpSchemaDataRepository implements SchemaDataRepository {

  static final String COLLECTION_NAME = "schemaData";
  static final String SRN_FIELD_NAME = "srn";

  Firestore firestore;

  public GcpSchemaDataRepository() {
    this.firestore = FirestoreOptions.getDefaultInstance().getService();
  }

  @Override
  public SchemaDataDto findBySrn(String srn) {
    log.debug("Requesting srn : {}", srn);
    log.debug("Log:{}", firestore);
    final ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(SRN_FIELD_NAME, srn).get();
    final QuerySnapshot querySnapshot;
    try {
      querySnapshot = query.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IngestException(String.format("Failed to SchemaData for srn %s", srn), e);
    }
    final List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new IngestException(String
          .format("Find by SRN returned %s document(s), expected 1, query srn : %s",
              documents.size(), srn));
    }

    SchemaDataDto schemaDataDTO =
        documents.size() == 0 ? null : documents.get(0).toObject(SchemaDataDto.class);
    log.debug("SRN request resulted in schema data : {}", schemaDataDTO);
    return schemaDataDTO;
  }

  @Override
  public void save(SchemaDataDto schemaData) {
    log.debug("Request to save schemaData : {}", schemaData);
    try {
      final WriteResult writeResult = firestore.collection(COLLECTION_NAME)
          .document(schemaData.getSrn())
          .set(schemaData).get();
      log.debug("SchemaData : {} saved on : {}", schemaData, writeResult.getUpdateTime());
    } catch (InterruptedException | ExecutionException e) {
      throw new IngestException(
          String.format("Exception during saving of schemaData : %s", schemaData), e);
    }
  }
}
