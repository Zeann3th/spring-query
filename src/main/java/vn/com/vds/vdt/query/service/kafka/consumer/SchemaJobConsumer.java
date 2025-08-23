package vn.com.vds.vdt.query.service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.query.dto.ParseSchemaRequest;
import vn.com.vds.vdt.query.service.parser.impl.SchemaParserFactory;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class SchemaJobConsumer {

    private final SchemaParserFactory schemaParserFactory;

    @KafkaListener(topics = "${app.kafka.topics.schema-parse}", groupId = "schema-parser")
    public void consume(ParseSchemaRequest request, @Header(KafkaHeaders.RECEIVED_KEY) String jobId) {
        log.info("Processing schema parse job {}", jobId);
        try {
            var parser = schemaParserFactory.getParser(request.getType());
            parser.parse(request);
            log.info("Schema parse job {} completed successfully", jobId);
        } catch (Exception e) {
            log.error("Schema parse job {} failed: {}", jobId, e.getMessage(), e);
        }
    }
}