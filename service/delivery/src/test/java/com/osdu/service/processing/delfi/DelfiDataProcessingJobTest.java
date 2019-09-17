package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;

import java.util.HashMap;
import java.util.Map;

import static com.osdu.service.processing.delfi.DelfiDataProcessingJob.FILE_LOCATION_KEY;
import static com.osdu.service.processing.delfi.DelfiDataProcessingJob.LOCATION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiDataProcessingJobTest {

  @Mock
  private SrnMappingService srnMappingService;

  @Mock
  private PortalService portalService;

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String SRN = "srn";
  private static final String ODES_ID = "odesId";
  private static final String SIGNED_URL = "signedUrl";

  private DelfiDataProcessingJob dataProcessingJob;

  @Before
  public void init() {
    dataProcessingJob = new DelfiDataProcessingJob(SRN, srnMappingService, portalService,
        AUTHORIZATION_TOKEN, PARTITION);
  }

  @Test
  public void testNoLocation() {
    // given
    when(srnMappingService.mapSrnToKind(eq(SRN))).thenReturn(ODES_ID);

    Record record = new Record() {
    };
    Map<String, Object> data = new HashMap<>();
    data.put("one", "test");

    Map<String, Object> details = new HashMap<>();
    details.put("two", "test");

    record.setDetails(details);
    record.setData(data);

    when(portalService.getRecord(eq(ODES_ID), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(record);

    // when
    ProcessingResult result = dataProcessingJob.call();

    // then
    assertThat(result.getProcessingResultStatus()).isEqualTo(ProcessingResultStatus.DATA);
    assertThat(result.getFileLocation()).isNull();
    assertThat(result.getSrn()).isEqualTo(SRN);
    assertThat(result.getData()).isEqualTo(record);
  }


  @Test
  public void testWithFileLocation() {
    // given
    when(srnMappingService.mapSrnToKind(eq(SRN))).thenReturn(ODES_ID);

    Record record = new Record() {
    };
    Map<String, Object> data = new HashMap<>();
    data.put(LOCATION_KEY, "test location");
    Map<String, Object> details = new HashMap<>();
    details.put("two", "test");
    record.setDetails(details);
    record.setData(data);
    when(portalService.getRecord(eq(ODES_ID), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(record);

    Map<String, Object> fileRecordDetails = new HashMap<>();
    fileRecordDetails.put(FILE_LOCATION_KEY, SIGNED_URL);
    fileRecordDetails.put("test", "test");
    FileRecord fileRecord = new FileRecord() {
    };
    fileRecord.setDetails(fileRecordDetails);
    when(portalService.getFile(eq("test location"), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(fileRecord);

    // when
    ProcessingResult result = dataProcessingJob.call();

    // then
    assertThat(result.getProcessingResultStatus()).isEqualTo(ProcessingResultStatus.FILE);
    assertThat(result.getFileLocation()).isEqualTo(SIGNED_URL);
    assertThat(result.getSrn()).isEqualTo(SRN);
    assertThat(result.getData()).isEqualTo(fileRecord);
  }

  @Test
  public void testNoMapping() {
    // given
    when(srnMappingService.mapSrnToKind(eq(SRN))).thenReturn(null);

    // when
    ProcessingResult result = dataProcessingJob.call();

    // then
    assertThat(result.getProcessingResultStatus()).isEqualTo(ProcessingResultStatus.NO_MAPPING);
    assertThat(result.getFileLocation()).isNull();
    assertThat(result.getSrn()).isEqualTo(SRN);
    assertThat(result.getData()).isNull();
  }
}
