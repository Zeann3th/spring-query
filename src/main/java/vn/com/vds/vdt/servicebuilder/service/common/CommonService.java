package vn.com.vds.vdt.servicebuilder.service.common;

public interface CommonService {
    void sendJob(String topic, String jobId, Object payload);
}