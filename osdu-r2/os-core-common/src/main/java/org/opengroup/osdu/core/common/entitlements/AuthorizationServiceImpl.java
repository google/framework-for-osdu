/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.entitlements;

import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.core.common.http.HeadersUtil;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Lazy
public class AuthorizationServiceImpl implements IAuthorizationService {

    private static final String TENANT_GROUP_FORMAT = "@%s";

    @Inject
    private IEntitlementsFactory factory;
    @Inject
    @Lazy
    private JaxRsDpsLog jaxRsDpsLog;

    @Override
    public AuthorizationResponse authorizeAny(DpsHeaders headers, String... roles) {
        AuthorizationResponse authorizationResponse = null;
        IEntitlementsService service = factory.create(headers);
        try {
            authorizationResponse = authorizeAny(headers, service.getGroups(), roles);
        } catch (EntitlementsException e) {
            handleEntitlementsException(e, headers);
        }
        return authorizationResponse;
    }

    @Override
    public AuthorizationResponse authorizeAny(String tenantName, DpsHeaders headers, String... roles) {
        IEntitlementsService service = factory.create(headers);
        AuthorizationResponse authorizationResponse = null;
        try {
            Groups groups = service.getGroups();
            List<GroupInfo> allGroups = new ArrayList<>(groups.getGroups());
            groups.setGroups(groups.getGroups().stream().filter(groupInfo -> groupInfo.getEmail()
                    .contains(String.format(TENANT_GROUP_FORMAT, tenantName))).collect(Collectors.toList()));

            authorizationResponse = authorizeAny(headers, groups, roles);
            groups.setGroups(allGroups);
        } catch (EntitlementsException e) {
            handleEntitlementsException(e, headers);
        }
        return authorizationResponse;
    }

    private void handleEntitlementsException(EntitlementsException e, DpsHeaders headers) {
        HttpResponse response = e.getHttpResponse();
        throw new AppException(response.getResponseCode(), "Access denied", "The user is not authorized to perform this action", HeadersUtil.toLogMsg(headers, null), e);
    }

    private AuthorizationResponse authorizeAny(DpsHeaders headers, Groups groups, String... roles) {
        String userEmail = null;
        List<String> logMessages = new ArrayList<>();
        Long curTimeStamp = System.currentTimeMillis();
        Long latency = System.currentTimeMillis() - curTimeStamp;

        logMessages.add(String.format("entitlements-api latency: %s", latency));
        logMessages.add(String.format("groups: %s", getEmailFromGroups(groups)));
        if (groups != null) {
            userEmail = groups.getMemberEmail();
            if (groups.any(roles)) {
                return AuthorizationResponse.builder().user(userEmail).groups(groups).build();
            }
        }
        jaxRsDpsLog.info(String.join(" | ", logMessages));
        jaxRsDpsLog.info(HeadersUtil.toLogMsg(headers, userEmail));
        throw AppException.createUnauthorized("required search service roles are missing for user");
    }

    private String getEmailFromGroups(Groups groups) {
        if (groups == null) return "";
        return groups.getGroups().stream().map(GroupInfo::getEmail).collect(Collectors.joining(" | "));
    }
}

