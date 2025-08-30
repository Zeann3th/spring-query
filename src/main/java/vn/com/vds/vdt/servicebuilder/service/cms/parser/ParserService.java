package vn.com.vds.vdt.servicebuilder.service.cms.parser;

import org.springframework.web.multipart.MultipartFile;
import vn.com.vds.vdt.servicebuilder.controller.dto.parser.ParseSchemaRequest;

public interface ParserService {
    void parse(ParseSchemaRequest request);

    void parseV2(MultipartFile file, String type);
}
