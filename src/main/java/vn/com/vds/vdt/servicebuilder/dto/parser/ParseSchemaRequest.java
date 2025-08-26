package vn.com.vds.vdt.servicebuilder.dto.parser;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParseSchemaRequest {
    private String type;
    private Metadata metadata;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Metadata {
        private String url;
        private String username;
        private String password;
        private String content;
    }
}
