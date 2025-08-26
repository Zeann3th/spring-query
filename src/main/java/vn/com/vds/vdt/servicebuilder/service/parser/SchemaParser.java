package vn.com.vds.vdt.servicebuilder.service.parser;

import vn.com.vds.vdt.servicebuilder.dto.parser.ParseSchemaRequest;

public interface SchemaParser {
    String getType();
    void parse(ParseSchemaRequest request);
}

