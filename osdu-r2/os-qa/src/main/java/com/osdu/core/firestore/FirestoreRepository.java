package com.osdu.core.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirestoreRepository {
    final Firestore firestore;

    public static final String INGEST_JOB_COLLECTION = "ingestJob";
    public static final String RECORD_BY_SRN_COLLECTION = "recordBySrn";
    public static final String SCHEMA_DATA_COLLECTION = "schemaData";
    public static final String COLLECTION_NAME = "file-locations";

    public FirestoreRepository() {
        this.firestore = FirestoreOptions.getDefaultInstance().getService();
    }

    public List<Map<String, Object>> findBy(String collection, String field, String value) {
        final ApiFuture<QuerySnapshot> query = firestore.collection(collection)
                .whereEqualTo(field, value).get();
        final QuerySnapshot querySnapshot;
        try {
            querySnapshot = query.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format("Failed to find data from collection - %s, field - %s, value -%s",
                    collection, field, value), e);
        }
        final List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

        return documents.stream().map(QueryDocumentSnapshot::getData)
                .collect(Collectors.toList());
    }

    public WriteResult save(String collection, Map<String, Object> data) {
        try {
            return firestore.collection(collection)
                    .document()
                    .set(data).get();

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    String.format("Exception during saving to collection - %s, data -%s", collection,
                            data), e);
        }
    }
}
