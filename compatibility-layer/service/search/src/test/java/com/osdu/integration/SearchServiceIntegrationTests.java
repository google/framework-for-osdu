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

package com.osdu.integration;

import static com.osdu.service.DelfiSearchService.KIND_HEADER_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.client.delfi.DelfiSearchClient;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.delfi.geo.ByBoundingBox;
import com.osdu.model.delfi.geo.SpatialFilter;
import com.osdu.model.osdu.GeoLocation;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.OsduSearchResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SearchServiceIntegrationTests {

  private static final String AUTHENTICATION = "auth";
  private static final String PARTITION = "partition";
  private static final String APP_KEY = "appKey";
  private static final String KIND = "kind";
  private static final int LIMIT = 3;
  private static final int OFFSET = 2;
  private static final String TEST = "test";

  @MockBean
  private DelfiPortalProperties portalProperties;
  @MockBean
  private DelfiSearchClient delfiSearchClient;
  @MockBean
  private AuthenticationService authenticationService;
  @Autowired
  private MockMvc mockMvc;
  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void shouldDeliverRecords() throws Exception {

    // given
    when(portalProperties.getAppKey()).thenReturn(APP_KEY);
    SpatialFilter spatialFilter = new SpatialFilter();
    List<List<Double>> coordinates = Arrays
        .asList(Arrays.asList(12.3, 23.4), Arrays.asList(34.5, 45.6));
    spatialFilter.setByBoundingBox(new ByBoundingBox(coordinates));

    DelfiSearchResult delfiSearchResult = new DelfiSearchResult();
    delfiSearchResult.setTotalCount(LIMIT);
    HashMap<Object, Object> data = new HashMap<>();
    data.put(TEST, TEST);
    delfiSearchResult.setResults(data);

    when(delfiSearchClient
        .searchIndex(eq(AUTHENTICATION), eq(APP_KEY), eq(PARTITION), any(DelfiSearchObject.class)))
        .thenReturn(delfiSearchResult);

    OsduSearchObject inputSearchObject = new OsduSearchObject();
    inputSearchObject.setStart(OFFSET);
    inputSearchObject.setCount(LIMIT);
    GeoLocation geoLocation = new GeoLocation();
    geoLocation.setType("byBoundingBox");
    geoLocation.setCoordinates(coordinates);
    inputSearchObject.setGeoLocation(geoLocation);
    Map<String, List<String>> metadata = new HashMap<>();
    metadata.put("key", Arrays.asList("value1", "value2"));
    inputSearchObject.setMetadata(metadata);

    HttpHeaders headers = new HttpHeaders();
    headers.add(OsduHeader.AUTHORIZATION, AUTHENTICATION);
    headers.add(OsduHeader.PARTITION, PARTITION);
    headers.add(KIND_HEADER_KEY, KIND);

    OsduSearchResult expected = new OsduSearchResult();
    expected.setTotalHits(LIMIT);
    expected.setCount(LIMIT);
    expected.setStart(OFFSET);
    expected.setResults(data);

    // when
    ResponseEntity responseEntity = (ResponseEntity) mockMvc
        .perform(MockMvcRequestBuilders.post("/")
            .headers(headers)
            .content(mapper.writeValueAsString(inputSearchObject)))
        .andExpect(status().isOk())
        .andReturn().getAsyncResult();

    // then
    OsduSearchResult response = (OsduSearchResult) responseEntity.getBody();

    assertEquals(response, expected);
  }
}
