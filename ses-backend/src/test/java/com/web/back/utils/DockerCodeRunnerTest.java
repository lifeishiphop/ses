package com.web.back.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.back.config.JudgeProperties;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DockerCodeRunnerTest {
    private final DockerCodeRunner runner = new DockerCodeRunner(new JudgeProperties(), new ObjectMapper());

    @Test
    void validatesSourceSizeAsUtf8BytesBeforeStartingDocker() {
        assertThatThrownBy(() -> runner.run(1, "가".repeat(22_000), List.of("")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.run(1, " ", List.of("")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidLanguagesAndUnboundedTestInputs() {
        assertThatThrownBy(() -> runner.run(0, "source", List.of("")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.run(1, "source", List.of("x".repeat(65_537))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.run(1, "source", Collections.nCopies(21, "")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
