// Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.entitlements;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.entitlements.CreateGroup;
import org.opengroup.osdu.core.common.model.entitlements.GetMembers;
import org.opengroup.osdu.core.common.model.entitlements.GroupEmail;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.entitlements.MemberInfo;
import org.opengroup.osdu.core.common.model.entitlements.Members;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;

public class EntitlementsService implements IEntitlementsService {
    private final String rootUrl;
    private final IHttpClient httpClient;
    private final DpsHeaders headers;

    EntitlementsService(EntitlementsAPIConfig config,
                        IHttpClient httpClient,
                        DpsHeaders headers) {
        this.rootUrl = config.getRootUrl();
        this.httpClient = httpClient;
        this.headers = headers;
        if (config.apiKey != null) {
            headers.put("AppKey", config.apiKey);
        }
    }

    @Override
    public MemberInfo addMember(GroupEmail groupEmail, MemberInfo memberInfo) throws EntitlementsException {
        String path = String.format("/groups/%s/members", groupEmail.getGroupEmail());
        String url = this.createUrl(path);
        HttpResponse result = this.httpClient.send(
                HttpRequest.post(memberInfo).url(url).headers(this.headers.getHeaders()).build());
        return this.getResult(result, MemberInfo.class);
    }

    @Override
    public Members getMembers(GroupEmail groupEmail, GetMembers getMembers) throws EntitlementsException {
        String path = String.format("/groups/%s/members?cursor=%s&limit=%s&role=%s",
                groupEmail.getGroupEmail(), getMembers.getCursor(), getMembers.getLimit(), getMembers.getRole());
        String url = this.createUrl(path);

        HttpResponse result = this.httpClient.send(
                HttpRequest.get().url(url).headers(this.headers.getHeaders()).build());
        return this.getResult(result, Members.class);
    }

    @Override
    public Groups getGroups() throws EntitlementsException {
        String path = String.format("/groups");
        String url = this.createUrl(path);
        HttpRequest rq = HttpRequest.get().url(url).headers(this.headers.getHeaders()).build();
        HttpResponse result = this.httpClient.send(rq);
        Groups output = this.getResult(result, Groups.class);
        return output;
    }

    @Override
    public GroupInfo createGroup(CreateGroup group) throws EntitlementsException {
        String url = this.createUrl("/groups");
        HttpResponse result = this.httpClient.send(
                HttpRequest.post(group).url(url).headers(this.headers.getHeaders()).build());
        GroupInfo output = this.getResult(result, GroupInfo.class);
        return output;
    }

    @Override
    public void deleteMember(String groupEmail, String memberEmail) throws EntitlementsException {
        String url = this.createUrl(String.format("/groups/%s/members/%s", groupEmail, memberEmail));
        HttpResponse result = this.httpClient.send(
                HttpRequest.delete().url(url).headers(this.headers.getHeaders()).build());
        this.getResult(result, String.class);
    }

    @Override
    public Groups authorizeAny(String... groupNames) throws EntitlementsException {
        Groups groups = this.getGroups();
        if (groups.any(groupNames)) {
            return groups;
        } else {
            throw new EntitlementsException(
                    String.format("User is unauthorized. %s does not belong to any of the given groups %s",
                            groups.getMemberEmail(), groupNames),
                    null);
        }
    }

    @Override
    public void authenticate() throws EntitlementsException {
        String path = String.format("/auth/validate");
        String url = this.createUrl(path);
        HttpRequest rq = HttpRequest.get().url(url).headers(this.headers.getHeaders()).build();
        HttpResponse result = this.httpClient.send(rq);
        this.getResult(result, Object.class);
    }

    private EntitlementsException generateEntitlementsException(HttpResponse result) {
        return new EntitlementsException(
                "Error making request to Entitlements service. Check the inner HttpResponse for more info.", result);
    }

    private String createUrl(String pathAndQuery) {
        return StringUtils.join(this.rootUrl, pathAndQuery);
    }

    private <T> T getResult(HttpResponse result, Class<T> type) throws EntitlementsException {
        if (result.isSuccessCode()) {
            try {
                return result.parseBody(type);
            } catch (JsonSyntaxException e) {
                throw new EntitlementsException("Error parsing response. Check the inner HttpResponse for more info.",
                        result);
            }
        } else {
            throw this.generateEntitlementsException(result);
        }
    }

}
