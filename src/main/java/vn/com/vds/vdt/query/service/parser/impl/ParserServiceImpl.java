package vn.com.vds.vdt.query.service.parser.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.query.common.constants.ErrorCodes;
import vn.com.vds.vdt.query.controller.dto.parser.ParseSchemaRequest;
import vn.com.vds.vdt.query.exception.CommandException;
import vn.com.vds.vdt.query.service.common.CommonService;
import vn.com.vds.vdt.query.service.parser.ParserService;

import java.util.UUID;

@SuppressWarnings("all")
@Service
@Slf4j
@RequiredArgsConstructor
public class ParserServiceImpl implements ParserService {
    private final SchemaParserFactory schemaParserFactory;
    private final CommonService commonService;

    @Value("${app.kafka.topics.schema-parse}")
    private String schemaParseTopic;

    @Override
    public void parse(ParseSchemaRequest request) {
        log.info("Request body: {}", request);
        String jobId = UUID.randomUUID().toString();
        try {
            schemaParserFactory.getParser(request.getType());
        } catch (IllegalArgumentException e) {
            throw new CommandException(ErrorCodes.QS00002, e.getMessage());
        }
        commonService.sendJob(schemaParseTopic, jobId, request);
        log.info("Schema parse job {} enqueued to Kafka", jobId);
        throw new CommandException(ErrorCodes.QS00001, "Schema parse job queued successfully");
    }
}