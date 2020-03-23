package org.opengroup.osdu.core.common.service.entitlements;

import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.entitlements.AuthorizationServiceImpl;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceImplTest {

    @Mock
    IEntitlementsFactory entitlementsFactory;
    @Mock
    IEntitlementsService service;
    @Mock
    JaxRsDpsLog log;

    @InjectMocks
    AuthorizationServiceImpl sut;

    @Before
    public void setup(){
        when(entitlementsFactory.create(any())).thenReturn(service);
    }

    @Test
    public void should_returnUser_when_ussrHasPermission()throws EntitlementsException {
        sut = createSut("service.legal.user");

        AuthorizationResponse result = sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.user");

        assertEquals("akelham@bbc.com", result.getUser());
    }

    @Test
    public void should_returnUser_when_ussrHasAnyPermission()throws EntitlementsException{
        sut = createSut("service.legal.editor");

        AuthorizationResponse result = sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.user", "service.legal.editor");

        assertEquals("akelham@bbc.com", result.getUser());
    }

    @Test
    public void should_throwForbidden_when_userDoesNotHaveRequiredPermission()throws EntitlementsException {
        sut = createSut("service.legal.user");

        try {
            sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.editor");
            fail("expected exception");
        }catch(AppException ex){
            assertEquals(401, ex.getError().getCode());
        }
    }

    @Test
    public void should_throwServerError_when_getGroupsThrowsServerError()throws EntitlementsException{
        sut = createSut("service.legal.user");
        HttpResponse response = new HttpResponse();
        response.setResponseCode(500);
        when(service.getGroups()).thenThrow(new EntitlementsException("", response));
        try {
            sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.editor");
            fail("expected exception");
        }catch(AppException ex){
            assertEquals(500, ex.getError().getCode());
        }
    }

    @Test
    public void should_throw400AppError_when_getGroupsThrows400EntitlementsError()throws EntitlementsException{
        sut = createSut("service.legal.user");
        HttpResponse response = new HttpResponse();
        response.setResponseCode(400);
        when(service.getGroups()).thenThrow(new EntitlementsException("", response));
        try {
            sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.editor");
            fail("expected exception");
        }catch(AppException ex){
            assertEquals(400, ex.getError().getCode());
        }
    }

    private AuthorizationServiceImpl createSut(String... roles) throws EntitlementsException {
        List<GroupInfo> groupInfos = new ArrayList<>();

        for(String s : roles) {
            GroupInfo group = new GroupInfo();
            group.setName(s);
            groupInfos.add(group);
        }
        Groups output = new Groups();
        output.setMemberEmail("akelham@bbc.com");
        output.setDesId("akelham@bbc.com");
        output.setGroups(groupInfos);

        when(service.getGroups()).thenReturn(output);

        return sut;
    }


}
