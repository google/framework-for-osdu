package org.opengroup.osdu.core.common.model.http;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScope
@Component
public class RequestInfo {
    @Inject
    private HttpServletRequest httpRequest;
    @Inject
    private ITenantFactory tenantStorage;
    @Inject
    private ServiceConfig config;
    @Inject
    private DpsHeaders dpsHeaders;

    private static final String expectedHeaderValue = "true";

    @Inject
    public  RequestInfo(HttpServletRequest request) {
        this.httpRequest = request;
    }

    public DpsHeaders getHeaders() {
        if(this.dpsHeaders == null && this.httpRequest != null) {
            Map<String, String> headers = Collections.list(httpRequest
                                                     .getHeaderNames())
                                                     .stream()
                                                     .collect(Collectors.toMap(h -> h, httpRequest::getHeader));
            this.dpsHeaders = DpsHeaders.createFromMap(headers);
            this.dpsHeaders.addCorrelationIdIfMissing();
        }
        return this.dpsHeaders;
    }

    public void setHeaders(DpsHeaders headers) {
        dpsHeaders = headers;
    }

    public TenantInfo getTenantInfo() {
        DpsHeaders sh = getHeaders();
        if(sh == null || Strings.isNullOrEmpty(sh.getPartitionIdWithFallbackToAccountId()))
            return null;
        return tenantStorage.getTenantInfo(sh.getPartitionIdWithFallbackToAccountId());
    }

    public boolean isHttps() {
        return getUri().startsWith("https") || 
        "https".equalsIgnoreCase(httpRequest.getHeader("x-forwarded-proto"));
    }

    public String getUri() {
        StringBuilder requestURL = new StringBuilder(httpRequest.getRequestURL().toString());
        String queryString = httpRequest.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    public String getComplianceRuleSet() {
        return getTenantInfo().getComplianceRuleSet();
    }

    public String getUser() {
        return getHeaders().getUserEmail();
    }

    public String getUserIp() {
        return httpRequest.getRemoteAddr();
    }

    public boolean isCronRequest() {
        String appEngineCronHeader = httpRequest.getHeader("X-Appengine-Cron");
        return expectedHeaderValue.equalsIgnoreCase(appEngineCronHeader) &&
                config.getCronIpAddress().equalsIgnoreCase(getUserIp());

    }
}
