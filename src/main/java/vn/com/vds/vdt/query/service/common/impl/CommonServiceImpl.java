package vn.com.vds.vdt.query.service.common.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.query.service.common.CommonService;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class CommonServiceImpl implements CommonService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendJob(String topic, String jobId, Object payload) {
        kafkaTemplate.send(topic, jobId, payload);
    }
}