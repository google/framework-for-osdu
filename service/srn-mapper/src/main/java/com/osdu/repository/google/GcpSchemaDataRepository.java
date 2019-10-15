package com.osdu.repository.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.osdu.exception.SrnMappingException;
import com.osdu.model.dto.SchemaDataDto;
import com.osdu.repository.SchemaDataRepository;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class GcpSchemaDataRepository implements SchemaDataRepository {

  private static final String COLLECTION_NAME = "schemaData";
  private static final String SRN_FIELD_NAME = "srn";

  final Firestore firestore;

  public GcpSchemaDataRepository() {
    this.firestore = FirestoreOptions.getDefaultInstance().getService();
  }

  @Override
  public SchemaDataDto findExactByTypeId(String typeId) {
    log.debug("Requesting typeId: {}", typeId);
    final ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereEqualTo(SRN_FIELD_NAME, typeId).get();
    final QuerySnapshot querySnapshot;
    try {
      querySnapshot = query.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new SrnMappingException(String.format("Failed to SchemaData for typeId: %s", typeId),
          e);
    }
    final List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

    if (documents.size() > 1) {
      throw new SrnMappingException(String
          .format("Find by typeId returned %s document(s), expected 1, query typeId: %s",
              documents.size(), typeId));
    }

    SchemaDataDto schemaDataDto = documents.isEmpty() ? null : documents.get(0).toObject(SchemaDataDto.class);
    log.debug("TypeId request resulted in schema data : {}", schemaDataDto);
    return schemaDataDto;
  }

  @Override
  public SchemaDataDto findLastByTypeId(String typeId) {
    log.debug("Requesting the latest schema data by typeId: {}", typeId);
    ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
        .whereGreaterThanOrEqualTo("typeId", typeId)
        .whereLessThanOrEqualTo("typeId", typeId + "\uf8ff")
        .get();

    SchemaDataDto schemaDataDto;
    try {
       schemaDataDto = query.get().getDocuments().stream()
          .map(doc -> doc.toObject(SchemaDataDto.class))
          .max(Comparator.comparing(SchemaDataDto::getCreated))
          .orElseThrow(() -> new SrnMappingException(
              "Unable to find any Schema Data for typeId: " + typeId));
    } catch (InterruptedException | ExecutionException e) {
      throw new SrnMappingException("Failed to find Schema Data for typeId: " + typeId);
    }

    log.debug("TypeId request resulted in schema data : {}", schemaDataDto);
    return schemaDataDto;
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
      throw new SrnMappingException(
          String.format("Exception during saving of schemaData : %s", schemaData), e);
    }
  }
}