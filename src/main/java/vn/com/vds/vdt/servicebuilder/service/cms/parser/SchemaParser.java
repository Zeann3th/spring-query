package vn.com.vds.vdt.servicebuilder.service.cms.parser;

import vn.com.vds.vdt.servicebuilder.controller.dto.parser.ParseSchemaRequest;

public interface SchemaParser {
    String getType();
    void parse(ParseSchemaRequest request);
}

