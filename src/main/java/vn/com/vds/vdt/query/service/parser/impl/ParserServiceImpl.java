package vn.com.vds.vdt.query.service.parser.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.query.common.ErrorCodes;
import vn.com.vds.vdt.query.dto.ParseSchemaRequest;
import vn.com.vds.vdt.query.exception.CommandException;
import vn.com.vds.vdt.query.service.parser.ParserService;

@SuppressWarnings("all")
@Service
@Slf4j
@RequiredArgsConstructor
public class ParserServiceImpl implements ParserService {
    private final SchemaParserFactory schemaParserFactory;

    @Override
    public void parse(ParseSchemaRequest request) {
        log.info("Request body: {}", request);
        try {
            var parser = schemaParserFactory.getParser(request.getType());
            parser.parse(request);
        } catch (IllegalArgumentException e) {
            throw new CommandException(ErrorCodes.QS00002, e.getMessage());
        }
        throw new CommandException(ErrorCodes.QS00001, "Schema parsed successfully");
    }
}
