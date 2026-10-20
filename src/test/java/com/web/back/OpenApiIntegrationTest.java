package com.web.back;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;

    @Test
    void servesSwaggerPageAndAssetsWithoutAuthentication() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Swagger UI")));
        mvc.perform(get("/swagger-ui/swagger-ui.css")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk());
    }

    @Test
    void documentsEveryControllerMappingWithSummaryAndSuccessResponse() throws Exception {
        JsonNode paths = document().path("paths");
        int operationCount = 0;
        for (var entry : mappings.getHandlerMethods().entrySet()) {
            if (!entry.getValue().getBeanType().getPackageName().startsWith("com.web.back.controller")) continue;
            for (String path : entry.getKey().getPatternValues()) {
                for (var method : entry.getKey().getMethodsCondition().getMethods()) {
                    JsonNode operation = paths.path(path).path(method.name().toLowerCase());
                    assertThat(operation.isMissingNode()).as(method + " " + path).isFalse();
                    assertThat(operation.path("summary").asText()).isNotBlank();
                    assertThat(operation.path("responses").has("200")).isTrue();
                    operationCount++;
                }
            }
        }
        assertThat(operationCount).isGreaterThan(0);
        assertThat(paths.properties().stream().mapToInt(e -> e.getValue().size()).sum()).isEqualTo(operationCount);
    }

    @Test
    void describesJwtAndLeavesPublicOperationsWithoutSecurityRequirement() throws Exception {
        JsonNode doc = document();
        assertThat(doc.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(doc.path("security").get(0).has("bearerAuth")).isTrue();
        for (String path : new String[]{"/login", "/signup", "/logoutAccount", "/mail", "/mailVerify", "/findPassword"}) {
            String method = path.equals("/findPassword") ? "put" : "post";
            JsonNode security = doc.path("paths").path(path).path(method).path("security");
            assertThat(security.isArray() && security.isEmpty()).as(path + " public security").isTrue();
        }
    }

    @Test
    void describesRequestDtoAndConcreteObjectArrayAndEmptyResponses() throws Exception {
        JsonNode doc = document();
        JsonNode paths = doc.path("paths");
        assertThat(paths.path("/login").path("post").at("/requestBody/content/application~1json/schema/$ref").asText())
                .endsWith("/LoginDto");
        assertThat(paths.path("/login").path("post").at("/responses/200/content/application~1json/schema/$ref").asText())
                .endsWith("/TokenDto");
        JsonNode listSchema = paths.path("/problem").path("get").at("/responses/200/content/application~1json/schema");
        assertThat(listSchema.path("type").asText()).isEqualTo("array");
        assertThat(listSchema.at("/items/$ref").asText()).endsWith("/ProblemDto");
        assertThat(paths.path("/logoutAccount").path("post").at("/responses/200").has("content")).isFalse();
        assertThat(paths.path("/signup").path("post").at("/responses/400/content/application~1json/schema/$ref").asText())
                .endsWith("/ApiError");
    }

    private JsonNode document() throws Exception {
        return mapper.readTree(mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }
}
