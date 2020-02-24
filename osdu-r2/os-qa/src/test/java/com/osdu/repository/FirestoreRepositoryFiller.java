package com.osdu.repository;

import com.google.cloud.firestore.WriteResult;
import com.osdu.core.firestore.FirestoreRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;

public class FirestoreRepositoryFiller {
    static FirestoreRepository firestoreRepository = new FirestoreRepository();
    public static Map<String, Object> map = new HashMap<>();
    static final String SRN = "srn";
    static final String RECORD_ID = "recordId";

    public static void fillFirestoreRepositoryWithData(String recordId, String srn) {
        map.put(RECORD_ID, recordId);
        map.put(SRN, srn);

        List<Map<String, Object>> found = firestoreRepository.findBy(FirestoreRepository.RECORD_BY_SRN_COLLECTION, SRN, srn);
        if (found.isEmpty()) {
            WriteResult save = firestoreRepository.save(FirestoreRepository.RECORD_BY_SRN_COLLECTION, map);
        }
    }

    private static void shouldFind(String value) {
        List<Map<String, Object>> found = firestoreRepository.findBy(FirestoreRepository.RECORD_BY_SRN_COLLECTION, SRN, value);
        Assert.assertFalse(found.isEmpty());
    }
}