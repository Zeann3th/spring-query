package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.vds.vdt.servicebuilder.controller.dto.parser.ParseSchemaRequest;
import vn.com.vds.vdt.servicebuilder.service.cms.parser.ParserService;

@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/api/v1/cms")
@RequiredArgsConstructor
public class CmsController implements ParserService {
    private final ParserService parserService;

    @PostMapping(value = "/migrate")
    public void parse(@RequestBody ParseSchemaRequest request) {
        parserService.parse(request);
    }
}
