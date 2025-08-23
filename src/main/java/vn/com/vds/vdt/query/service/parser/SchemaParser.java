package vn.com.vds.vdt.query.service.parser;

import vn.com.vds.vdt.query.dto.ParseSchemaRequest;

public interface SchemaParser {
    String getType();
    void parse(ParseSchemaRequest request);
}

