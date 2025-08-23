package vn.com.vds.vdt.query.service.common;

public interface CommonService {
    void sendJob(String topic, String jobId, Object payload);
}