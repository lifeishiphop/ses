package com.web.back.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {
    private String dockerCommand = "docker";
    private String image = "ses-judge:local";
    private String memory = "512m";
    private Duration compileTimeout = Duration.ofSeconds(10);
    private Duration runTimeout = Duration.ofSeconds(10);
    private int maxConcurrent = 2;

    @PostConstruct
    void validate() {
        Assert.hasText(dockerCommand, "judge.docker-command가 필요합니다.");
        Assert.hasText(image, "judge.image가 필요합니다.");
        Assert.hasText(memory, "judge.memory가 필요합니다.");
        Assert.isTrue(compileTimeout != null && compileTimeout.toMillis() > 0, "컴파일 제한 시간은 양수여야 합니다.");
        Assert.isTrue(runTimeout != null && runTimeout.toMillis() > 0, "실행 제한 시간은 양수여야 합니다.");
        Assert.isTrue(maxConcurrent > 0, "동시 채점 수는 양수여야 합니다.");
    }
}
