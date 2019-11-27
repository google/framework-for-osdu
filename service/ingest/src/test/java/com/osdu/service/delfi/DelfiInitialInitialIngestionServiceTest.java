/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service.delfi;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osdu.exception.IngestException;
import com.osdu.model.IngestHeaders;
import com.osdu.model.IngestResult;
import com.osdu.model.type.manifest.LoadManifest;
import com.osdu.service.JobStatusService;
import com.osdu.service.validation.LoadManifestValidationService;
import java.util.HashMap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class DelfiInitialInitialIngestionServiceTest {

  private static final String JOB_ID = "jobId";

  @Mock
  JobStatusService jobStatusService;
  @Mock
  LoadManifestValidationService loadManifestValidationService;

  @InjectMocks
  private DelfiInitialIngestService delfiInitialIngestService;

  @Test
  public void shouldIngestManifest() {

    // given
    LoadManifest manifest = LoadManifest.builder().build();
    MessageHeaders headers = new MessageHeaders(new HashMap<>());

    when(jobStatusService.initInjectJob()).thenReturn(JOB_ID);

    // when
    IngestResult ingestResult = delfiInitialIngestService.ingestManifest(manifest, headers);

    // then
    IngestHeaders ingestHeaders = IngestHeaders.builder().build();
    assertThat(ingestResult.getJobId()).isEqualTo(JOB_ID);
  }

  @Test(expected = IngestException.class)
  public void shouldNotInitiateJobIfManifestValidationFail() {

    // given
    LoadManifest manifest = LoadManifest.builder().build();
    MessageHeaders headers = new MessageHeaders(new HashMap<>());

    // when
    delfiInitialIngestService.ingestManifest(manifest, headers);

    // then
    verify(jobStatusService, never()).initInjectJob();
  }
}
