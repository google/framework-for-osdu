package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.service.processing.ResultDataPostProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class DelfiResultDataPostProcessorTest {

  static final String THREE = "three";
  private ResultDataPostProcessor resultDataPostProcessor;

  @Before
  public void init() {
    List<String> filelsToStrip = new ArrayList<>();
    filelsToStrip.add("one");
    filelsToStrip.add(THREE);
    resultDataPostProcessor = new DelfiResultDataPostProcessor(filelsToStrip);
  }

  @Test
  public void testFileRecord() {
    // given
    FileRecord fileRecord = new FileRecord() {
    };

    Map<String, Object> testDetails = new HashMap<>();
    testDetails.put("one", "test");
    testDetails.put("two", "test");
    testDetails.put(THREE, "test");
    testDetails.put("four", "test");
    fileRecord.setDetails(testDetails);

    // when
    resultDataPostProcessor.processData(fileRecord);

    // then
    Map<String, Object> expectedTestDetails = new HashMap<>();
    expectedTestDetails.put("two", "test");
    expectedTestDetails.put("four", "test");
    assertThat(fileRecord.getDetails()).containsExactlyInAnyOrderEntriesOf(expectedTestDetails);
  }

  @Test
  public void testRecord() {

    // given
    Record record = new Record() {
    };

    Map<String, Object> testData = new HashMap<>();
    testData.put("one", "test");
    testData.put("two", "test");
    testData.put(THREE, "test");
    testData.put("four", "test");

    Map<String, Object> testDetails = new HashMap<>();
    testDetails.put("one", "test");
    testDetails.put("two", "test");

    record.setData(testData);
    record.setDetails(testDetails);

    // when
    resultDataPostProcessor.processData(record);

    // then
    Map<String, Object> expectedTestDetails = new HashMap<>();
    expectedTestDetails.put("two", "test");
    assertThat(record.getDetails()).containsExactlyInAnyOrderEntriesOf(expectedTestDetails);

    Map<String, Object> expectedTestData = new HashMap<>();
    expectedTestData.put("two", "test");
    expectedTestData.put("four", "test");
    assertThat(record.getData()).containsExactlyInAnyOrderEntriesOf(expectedTestData);
  }

  @Test
  public void testUnknown() {
    Object test = resultDataPostProcessor.processData("test");
    assertEquals("test", test);
  }

}
