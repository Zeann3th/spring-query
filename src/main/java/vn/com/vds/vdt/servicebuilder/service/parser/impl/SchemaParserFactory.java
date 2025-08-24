package vn.com.vds.vdt.servicebuilder.service.parser.impl;

import org.springframework.stereotype.Component;
import vn.com.vds.vdt.servicebuilder.service.parser.SchemaParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SchemaParserFactory {
    private final Map<String, SchemaParser> parserMap = new HashMap<>();

    public SchemaParserFactory(List<SchemaParser> parsers) {
        for (SchemaParser parser : parsers) {
            parserMap.put(parser.getType().toLowerCase(), parser);
        }
    }

    public SchemaParser getParser(String type) {
        SchemaParser parser = parserMap.get(type.toLowerCase());
        if (parser == null) {
            throw new IllegalArgumentException("No parser found for type: " + type);
        }
        return parser;
    }
}
