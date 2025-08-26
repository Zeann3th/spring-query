package vn.com.vds.vdt.servicebuilder.service.core;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("all")
public class DefaultJobWorker {
    @JobWorker(type = "default")
    public void execute(final JobClient client, final ActivatedJob job) {
        client.newCompleteCommand(job.getKey())
                .variables(job.getVariablesAsMap())
                .send()
                .join();
    }
}
