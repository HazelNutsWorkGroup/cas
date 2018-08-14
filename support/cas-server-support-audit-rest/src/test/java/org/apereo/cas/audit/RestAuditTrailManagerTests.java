package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.CasSupportRestAuditConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static junit.framework.TestCase.*;

/**
 * This is {@link RestAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasSupportRestAuditConfiguration.class,
    CasCoreUtilSerializationConfiguration.class
})
@TestPropertySource(properties = {
    "cas.audit.rest.url=http://localhost:9296",
    "cas.audit.rest.asynchronous=false"
})
@Getter
public class RestAuditTrailManagerTests extends BaseAuditConfigurationTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("restAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    private static String DATA;

    @BeforeClass
    public static void initialize() throws JsonProcessingException {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", new Date(), "123.456.789.000", "123.456.789.000");
        DATA = MAPPER.writeValueAsString(CollectionUtils.wrapSet(audit));
    }

    @Test
    @Override
    public void verifyAuditManager() {
        try (val webServer = new MockWebServer(9296,
            new ByteArrayResource(DATA.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyAuditManager();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
