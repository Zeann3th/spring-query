package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.parser.ParseSchemaRequest;
import vn.com.vds.vdt.servicebuilder.service.cms.parser.ParserService;

@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
@ResponseWrapper
public class CmsController implements ParserService {
    private final ParserService parserService;

    @PostMapping(value = "/v1/cms/migrate")
    public void parse(@RequestBody ParseSchemaRequest request) {
        parserService.parse(request);
    }

    @PostMapping(value = "/v2/cms/migrate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void parseV2(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type
    ) {
        parserService.parseV2(file, type);
    }
}
