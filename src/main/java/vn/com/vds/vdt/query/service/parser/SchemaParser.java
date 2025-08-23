package vn.com.vds.vdt.query.service.parser;

import vn.com.vds.vdt.query.controller.dto.parser.ParseSchemaRequest;

public interface SchemaParser {
    String getType();
    void parse(ParseSchemaRequest request);
}

