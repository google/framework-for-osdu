package com.osdu.service.delfi;


import static com.osdu.request.OsduHeader.RESOURCE_HOME_REGION_ID;
import static com.osdu.request.OsduHeader.RESOURCE_HOST_REGION_IDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.model.Record;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.manifest.ManifestFile;
import com.osdu.model.manifest.WorkProductComponent;
import com.osdu.service.PortalService;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;

@RunWith(MockitoJUnitRunner.class)
public class DelfiEnrichServiceTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String TEST_KEY = "test";
  private static final String TEST_VALUE = "test-value";
  private static final String RECORD_ID = "recordId";
  private static final String HOME_REGION_ID = "home_region_id";
  private static final String HOST_REGION_ID = "host_region_id";

  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private PortalService portalService;

  private DelfiEnrichService delfiEnrichService;

  @Before
  public void initialize() {
    delfiEnrichService = new DelfiEnrichService(objectMapper, portalService);
  }

  @Test
  public void shouldEnrichRecord() {

    // given
    HashMap<String, Object> data = new HashMap<>();
    data.put(TEST_KEY, TEST_VALUE);
    WorkProductComponent wpc = WorkProductComponent.builder()
        .data(data).build();
    ManifestFile manifestFile = ManifestFile.builder().wpc(wpc).build();
    SignedFile signedFile = SignedFile.builder().file(manifestFile).build();
    SubmittedFile file = SubmittedFile.builder().signedFile(signedFile).build();

    Record record = new Record();
    record.setData(new HashMap<>());

    when(portalService.getRecord(eq(RECORD_ID), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(record);
    when(portalService.putRecord(eq(record), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenAnswer(i -> i.getArguments()[0]);

    RequestMeta meta = RequestMeta.builder().authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION).build();

    Map<String, Object> headersMap = new HashMap<>();
    headersMap.put(RESOURCE_HOME_REGION_ID, HOME_REGION_ID);
    headersMap.put(RESOURCE_HOST_REGION_IDS, HOST_REGION_ID);
    MessageHeaders headers = new MessageHeaders(headersMap);

    // when
    EnrichedFile enrichedFile = delfiEnrichService.enrichRecord(file, meta, headers);

    // then
    Map<String, Object> enrichedData = enrichedFile.getRecord().getData();
    assertThat(enrichedData.get(RESOURCE_HOME_REGION_ID)).isEqualTo(HOME_REGION_ID);
    assertThat(enrichedData.get(RESOURCE_HOST_REGION_IDS)).isEqualTo(HOST_REGION_ID);
    assertThat(enrichedData.get(RESOURCE_HOST_REGION_IDS)).isEqualTo(HOST_REGION_ID);
    assertThat(enrichedData.get(TEST_KEY)).isEqualTo(TEST_VALUE);
  }
}
