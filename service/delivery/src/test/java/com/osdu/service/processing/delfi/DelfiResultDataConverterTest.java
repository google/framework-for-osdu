package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.service.processing.ResultDataConverter;
import com.osdu.service.processing.ResultDataPostProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DelfiResultDataConverterTest {

    @Mock
    private ResultDataPostProcessor resultDataPostProcessor;

    @InjectMocks
    private ResultDataConverter resultDataConverter = new DelfiResultDataConverter();

    @Test
    public void testDataResponse() {

        Map<String, Object> data = new HashMap<>();
        data.put("one", "test");
        data.put("two", "test");
        ProcessingResult dataResult = createProcessingResult(ProcessingResultStatus.DATA, null, data, "srn_1");

        DeliveryResponse response = resultDataConverter.convertProcessingResults(Collections.singletonList(dataResult));

        assertThat(response.getUnprocessedSrns()).isEmpty();
        assertThat(response.getResult()).hasSize(1);
        assertThat(response.getResult().get(0).getData()).isEqualTo(data);
        assertThat(response.getResult().get(0).getFileLocation()).isNull();
        assertThat(response.getResult().get(0).getSrn()).isEqualTo("srn_1");
    }

    @Test
    public void testAllResponses() {

        Map<String, Object> data = new HashMap<>();
        data.put("one", "test");
        data.put("two", "test");
        ProcessingResult dataResult = createProcessingResult(ProcessingResultStatus.DATA, null, data, "srn_1");

        ProcessingResult fileResult = createProcessingResult(ProcessingResultStatus.FILE, "http://url.com", data, "srn_2");

        ProcessingResult noMappingResult = createProcessingResult(ProcessingResultStatus.NO_MAPPING, null, null, "srn_3");

        List<ProcessingResult> results = Arrays.asList(dataResult, fileResult, noMappingResult);
        DeliveryResponse response = resultDataConverter.convertProcessingResults(results);

        assertThat(response.getResult()).hasSize(2);
        assertThat(response.getResult().get(0).getData()).isEqualTo(data);
        assertThat(response.getResult().get(0).getFileLocation()).isNull();
        assertThat(response.getResult().get(0).getSrn()).isEqualTo("srn_1");

        assertThat(response.getResult().get(1).getData()).isEqualTo(data);
        assertThat(response.getResult().get(1).getFileLocation()).isEqualTo("http://url.com");
        assertThat(response.getResult().get(1).getSrn()).isEqualTo("srn_2");

        assertThat(response.getUnprocessedSrns()).hasSize(1);
        assertThat(response.getUnprocessedSrns().get(0)).isEqualTo("srn_3");
    }

    private ProcessingResult createProcessingResult(ProcessingResultStatus status, String fileLocation, Object data, String srn) {
        ProcessingResult processingResult = new ProcessingResult();
        processingResult.setProcessingResultStatus(status);
        processingResult.setFileLocation(fileLocation);
        processingResult.setData(data);
        processingResult.setSrn(srn);

        return processingResult;
    }
}
