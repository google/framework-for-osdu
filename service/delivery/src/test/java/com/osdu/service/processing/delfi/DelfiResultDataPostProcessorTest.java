package com.osdu.service.processing.delfi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.osdu.model.BaseRecord;
import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.service.processing.ResultDataPostProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class DelfiResultDataPostProcessorTest {

  private static final String ONE = "one";
  private static final String TWO = "two";
  private static final String THREE = "three";
  private static final String FOUR = "four";
  private static final String TEST_VALUE = "test";

  private ResultDataPostProcessor resultDataPostProcessor;

  @Before
  public void init() {
    List<String> filelsToStrip = new ArrayList<>();
    filelsToStrip.add(ONE);
    filelsToStrip.add(THREE);
    resultDataPostProcessor = new DelfiResultDataPostProcessor(filelsToStrip);
  }

  @Test
  public void testFileRecord() {

    // given
    Map<String, Object> testDetails = new HashMap<>();
    testDetails.put(ONE, TEST_VALUE);
    testDetails.put(TWO, TEST_VALUE);
    testDetails.put(THREE, TEST_VALUE);
    testDetails.put(FOUR, TEST_VALUE);
    FileRecord fileRecord = new FileRecord();
    fileRecord.setAdditionalProperties(testDetails);

    // when
    resultDataPostProcessor.processData(fileRecord);

    // then
    Map<String, Object> expectedTestDetails = new HashMap<>();
    expectedTestDetails.put(TWO, TEST_VALUE);
    expectedTestDetails.put(FOUR, TEST_VALUE);
    assertThat(fileRecord.getAdditionalProperties())
        .containsExactlyInAnyOrderEntriesOf(expectedTestDetails);
  }

  @Test
  public void testRecord() {

    // given
    Map<String, Object> testData = new HashMap<>();
    testData.put(ONE, TEST_VALUE);
    testData.put(TWO, TEST_VALUE);
    testData.put(THREE, TEST_VALUE);
    testData.put(FOUR, TEST_VALUE);

    Map<String, Object> testDetails = new HashMap<>();
    testDetails.put(ONE, TEST_VALUE);
    testDetails.put(TWO, TEST_VALUE);

    Record record = new Record();
    record.setData(testData);
    record.setAdditionalProperties(testDetails);

    // when
    resultDataPostProcessor.processData(record);

    // then
    Map<String, Object> expectedTestDetails = new HashMap<>();
    expectedTestDetails.put(TWO, TEST_VALUE);
    assertThat(record.getAdditionalProperties())
        .containsExactlyInAnyOrderEntriesOf(expectedTestDetails);

    Map<String, Object> expectedTestData = new HashMap<>();
    expectedTestData.put(TWO, TEST_VALUE);
    expectedTestData.put(FOUR, TEST_VALUE);
    assertThat(record.getData()).containsExactlyInAnyOrderEntriesOf(expectedTestData);
  }

  @Test
  public void testUnknown() {
    BaseRecord br = new BaseRecord() {
    };
    Object test = resultDataPostProcessor.processData(br);
    assertEquals(br, test);
  }

}
