package com.ft.methodeimagesetmapper.health;

import com.ft.messagequeueproducer.health.QueueProxyHealthcheck;
import com.ft.platform.dropwizard.AdvancedResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CanConnectToMessageQueueProducerProxyHealthcheckTest {
    private CanConnectToMessageQueueProducerProxyHealthcheck healthcheck;

    @Mock
    private QueueProxyHealthcheck queueProxyHealthcheck;

    @Before
    public void setUp() {
        HealthcheckConfiguration healthcheckConfiguration = new HealthcheckConfiguration("kafka-proxy", 2, "business impact text", "tech summary", "panic guide url");
        healthcheck = new CanConnectToMessageQueueProducerProxyHealthcheck(queueProxyHealthcheck, healthcheckConfiguration, null);
    }

    @Test
    public void shouldReturnHealthyWhenProxyHealthcheckIsHealthy() throws Exception {
        when(queueProxyHealthcheck.check()).thenReturn(Optional.empty());

        AdvancedResult expectedHealthCheckResult = AdvancedResult.healthy("OK");
        AdvancedResult actualHealthCheckResult = healthcheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
        assertThat(actualHealthCheckResult.checkOutput(), containsString(expectedHealthCheckResult.checkOutput()));
    }

    @Test
    public void shouldReturnErrorWhenProxyHealthcheckContainsThrowable() throws Exception {
        QueueProxyHealthcheck.Unhealthy result = mock(QueueProxyHealthcheck.Unhealthy.class);
        String msg = "Test exception";
        Throwable exception = new RuntimeException(msg);
        when(result.getThrowable()).thenReturn(exception);
        when(queueProxyHealthcheck.check()).thenReturn(Optional.of(result));

        AdvancedResult expectedHealthCheckResult = AdvancedResult.error(healthcheck, msg);
        AdvancedResult actualHealthCheckResult = healthcheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
        assertThat(actualHealthCheckResult.checkOutput(), containsString(expectedHealthCheckResult.checkOutput()));
    }

    @Test
    public void shouldReturnErrorWhenProxyHealthcheckContainsMessage() throws Exception {
        QueueProxyHealthcheck.Unhealthy result = mock(QueueProxyHealthcheck.Unhealthy.class);
        String msg = "Test unhealthy result";
        when(result.getMessage()).thenReturn(msg);
        when(queueProxyHealthcheck.check()).thenReturn(Optional.of(result));

        AdvancedResult expectedHealthCheckResult = AdvancedResult.error(healthcheck, msg);
        AdvancedResult actualHealthCheckResult = healthcheck.checkAdvanced();

        assertThat(actualHealthCheckResult.status(), is(equalTo(expectedHealthCheckResult.status())));
        assertThat(actualHealthCheckResult.checkOutput(), containsString(expectedHealthCheckResult.checkOutput()));
    }
}
