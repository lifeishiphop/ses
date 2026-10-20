package com.web.back;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"API_DOCS_ENABLED=false",
                "spring.datasource.url=jdbc:h2:mem:ses-http-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR"})
@ActiveProfiles("test")
class HttpErrorIntegrationTest {
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper mapper;

    @Test
    void disabledDocumentationReturns404OverHttp() {
        for (String path : new String[]{"/v3/api-docs", "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-ui/index.html"}) {
            assertThat(http.getForEntity(path, String.class).getStatusCode()).as(path).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void authenticationFailureKeeps401AndStandardErrorBody() throws Exception {
        var response = http.getForEntity("/problem", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(mapper.readTree(response.getBody()).path("status").asInt()).isEqualTo(401);
    }
}
