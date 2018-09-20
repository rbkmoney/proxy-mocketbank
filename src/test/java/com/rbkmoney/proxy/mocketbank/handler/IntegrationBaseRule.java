package com.rbkmoney.proxy.mocketbank.handler;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import com.palantir.docker.compose.execution.DockerComposeExecArgument;
import com.palantir.docker.compose.execution.DockerComposeExecOption;
import org.junit.rules.ExternalResource;

import java.io.IOException;

public class IntegrationBaseRule extends ExternalResource {

    private static final String CDS = "cds";
    private static final String HOLMES = "holmes";
    private static final String PROXY_MOCKETBANK_MPI = "proxy-mocketbank-mpi";

    public static final DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose.yml")
            .waitingForService(CDS, HealthChecks.toHaveAllPortsOpen())
            .waitingForService(HOLMES, HealthChecks.toHaveAllPortsOpen())
            .waitingForService(PROXY_MOCKETBANK_MPI, HealthChecks.toHaveAllPortsOpen())
            .build();

    @Override
    protected void before() throws Throwable {
        docker.before();
        executeHolmesScripts();
    }

    @Override
    protected void after() {
        docker.after();
    }

    private void executeHolmesScripts() throws IOException, InterruptedException {
        docker.dockerCompose().exec(
                DockerComposeExecOption.options("-T"),
                HOLMES,
                DockerComposeExecArgument.arguments("/opt/holmes/scripts/cds/init-keyring.sh")
        );
    }

}
