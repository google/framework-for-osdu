// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.storage.jobs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MessageContent;
import org.opengroup.osdu.core.common.model.legal.jobs.ComplianceMessagePushReceiver;
import org.opengroup.osdu.core.common.model.legal.jobs.ILegalComplianceChangeService;
import org.opengroup.osdu.core.common.model.legal.jobs.LegalTagConsistencyValidator;
import org.opengroup.osdu.core.common.http.RequestBodyExtractor;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ComplianceMessagePushReceiverTest {
    private final String DATA_ENCODED = "eyJzdGF0dXNDaGFuZ2VkVGFncyI6W3siY2hhbmdlZFRhZ05hbWUiOiJiaWdvaWwtbGVnYWx0YWdzdGF0dXMtam9iIiwiY2hhbmdlZFRhZ1N0YXR1cyI6ImluY29tcGxpYW50In1dfQ";

    @InjectMocks
    private ComplianceMessagePushReceiver sut;

    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    private RequestBodyExtractor requestBodyExtractor;

    @Mock
    private LegalTagConsistencyValidator legalTagConsistencyValidator;

    @Mock
    private ILegalComplianceChangeService legalComplianceChangeService;

    @Test
    public void shouldThrowException_whenNoAccountIDInRequest() {
        MessageContent messageContent = new MessageContent();
        messageContent.setMessageId("testId");
        String decoded = new String(Base64.getDecoder().decode(this.DATA_ENCODED));

        messageContent.setData(decoded);

        Map<String, String> attributes = new HashMap<>();
        messageContent.setAttributes(attributes);

        this.sut.receiveMessageFromHttpRequest();
    }
}
