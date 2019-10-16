package com.osdu.service.processing.delfi;

import static org.assertj.core.api.Assertions.assertThat;

import com.osdu.model.BaseRecord;
import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.service.processing.ResultDataConverter;
import com.osdu.service.processing.ResultDataPostProcessor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiResultDataConverterTest {

  private static final String SRN_1 = "srn_1";
  private static final String ONE = "one";
  private static final String TWO = "two";
  private static final String TEST = "test";
  private static final String SRN_2 = "srn_2";
  private static final String SRN_3 = "srn_3";

  @Mock(answer = Answers.RETURNS_MOCKS)
  private ResultDataPostProcessor resultDataPostProcessor;

  @InjectMocks
  private ResultDataConverter resultDataConverter = new DelfiResultDataConverter();

  @Test
  public void shouldConvertDataRecordResult() {

    // given
    Map<String, Object> data = new HashMap<>();
    data.put(ONE, TEST);
    data.put(TWO, TEST);
    Record record = new Record() {
    };
    record.setData(data);

    ProcessingResult dataResult = createProcessingResult(ProcessingResultStatus.DATA, null, record,
        SRN_1);

    // when
    DeliveryResponse response = resultDataConverter
        .convertProcessingResults(Collections.singletonList(dataResult));

    // then
    assertThat(response.getUnprocessedSrns()).isEmpty();
    assertThat(response.getResult()).hasSize(1);
    assertThat(((Record) response.getResult().get(0).getData()).getData()).isEqualTo(data);
    assertThat(response.getResult().get(0).getFileLocation()).isNull();
    assertThat(response.getResult().get(0).getSrn()).isEqualTo(SRN_1);
  }

  @Test
  public void shouldConvertDataRecordFileRecordAndNoMappingResults() {

    // given
    Map<String, Object> data = new HashMap<>();
    data.put(ONE, TEST);
    data.put(TWO, TEST);
    Record record = new Record() {
    };
    record.setData(data);
    ProcessingResult dataResult = createProcessingResult(ProcessingResultStatus.DATA, null, record,
        SRN_1);

    FileRecord fileRecord = new FileRecord() {
    };
    fileRecord.setDetails(data);
    ProcessingResult fileResult = createProcessingResult(ProcessingResultStatus.FILE,
        "http://url.com", fileRecord, SRN_2);

    ProcessingResult noMappingResult = createProcessingResult(ProcessingResultStatus.NO_MAPPING,
        null, null, SRN_3);

    List<ProcessingResult> results = Arrays.asList(dataResult, fileResult, noMappingResult);

    // when
    DeliveryResponse response = resultDataConverter.convertProcessingResults(results);

    // then
    assertThat(response.getResult()).hasSize(2);
    assertThat(((Record) response.getResult().get(0).getData()).getData()).isEqualTo(data);
    assertThat(response.getResult().get(0).getFileLocation()).isNull();
    assertThat(response.getResult().get(0).getSrn()).isEqualTo(SRN_1);

    assertThat(((FileRecord) response.getResult().get(1).getData()).getDetails()).isEqualTo(data);
    assertThat(response.getResult().get(1).getFileLocation()).isEqualTo("http://url.com");
    assertThat(response.getResult().get(1).getSrn()).isEqualTo(SRN_2);

    assertThat(response.getUnprocessedSrns()).hasSize(1);
    assertThat(response.getUnprocessedSrns().get(0)).isEqualTo(SRN_3);
  }

  private ProcessingResult createProcessingResult(ProcessingResultStatus status,
      String fileLocation, BaseRecord data, String srn) {
    ProcessingResult processingResult = new ProcessingResult();
    processingResult.setProcessingResultStatus(status);
    processingResult.setFileLocation(fileLocation);
    processingResult.setData(data);
    processingResult.setSrn(srn);

    return processingResult;
  }
}
