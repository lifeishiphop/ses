package com.web.back.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.back.config.JudgeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@Tag("docker")
class DockerJudgeIntegrationTest {
    private JudgeProperties properties;
    private DockerCodeRunner runner;

    @BeforeEach
    void setup() {
        properties = new JudgeProperties();
        properties.setRunTimeout(Duration.ofSeconds(3));
        runner = new DockerCodeRunner(properties, new ObjectMapper());
    }

    @Test
    void compilesJavaOnceAndSuppliesMultipleInputsThroughStdin() {
        String source = """
                import java.util.Scanner;
                public class Main {
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);
                        System.err.println("debug");
                        System.out.println(scanner.nextInt() + scanner.nextInt());
                    }
                }
                """;
        var result = runner.run(1, source, List.of("1 2\r\n", "20 30\n"));
        assertThat(result.status()).isEqualTo("OK");
        assertThat(result.cases()).extracting(DockerCodeRunner.CaseResult::status).containsExactly("OK", "OK");
        assertThat(result.cases()).extracting(DockerCodeRunner.CaseResult::output).containsExactly("3\n", "50\n");
        assertThat(result.cases().get(0).error()).contains("debug");
    }

    @Test
    void compilesCppAndPreservesUnicodeQuotesAndShellMetacharacters() {
        String source = """
                #include <iostream>
                #include <string>
                int main() { std::string line; std::getline(std::cin, line); std::cout << line; }
                """;
        String input = "한글 ' \" $(echo injected) ; & \\ 경로";
        var result = runner.run(2, source, List.of(input));
        assertThat(result.cases().get(0).status()).isEqualTo("OK");
        assertThat(result.cases().get(0).output()).isEqualTo(input);
    }

    @Test
    void reportsCompilerFailure() {
        assertThat(runner.run(1, "public class Main { broken }", List.of("")).status()).isEqualTo("COMPILE_ERROR");
        assertThat(runner.run(2, "broken C++", List.of("")).status()).isEqualTo("COMPILE_ERROR");
    }

    @Test
    void reportsRuntimeFailure() {
        var result = runner.run(2, "int main() { return 7; }", List.of(""));
        assertThat(result.cases().get(0).status()).isEqualTo("RUNTIME_ERROR");
    }

    @Test
    void terminatesInfiniteLoopAndThenAcceptsAnotherSubmission() {
        properties.setRunTimeout(Duration.ofMillis(300));
        var result = runner.run(2, "int main() { for (;;) {} }", List.of(""));
        assertThat(result.cases().get(0).status()).isEqualTo("TIME_LIMIT");
        assertThat(runner.run(2, "int main() { return 0; }", List.of("")).cases().get(0).status()).isEqualTo("OK");
    }

    @Test
    void enforcesCompilationTimeout() {
        properties.setCompileTimeout(Duration.ofMillis(1));
        assertThat(runner.run(1, "public class Main {}", List.of("")).status()).isEqualTo("TIME_LIMIT");
    }

    @Test
    void boundsExcessiveOutput() {
        var result = runner.run(2, "#include <iostream>\nint main() { for (;;) std::cout << \"xxxxxxxxxxxxxxxx\"; }", List.of(""));
        assertThat(result.cases().get(0).status()).isEqualTo("OUTPUT_LIMIT");
        assertThat(result.cases().get(0).output().getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isLessThanOrEqualTo(65_536);
    }

    @Test
    void isolatesTestFilesAndFreezesCompiledArtifacts() {
        String source = """
                #include <fstream>
                #include <iostream>
                #include <filesystem>
                #include <unistd.h>
                int main() {
                    std::cout << std::filesystem::exists("marker") << " " << (getuid() != 0) << " ";
                    std::ofstream frozen("/workspace/build/Main");
                    std::cout << frozen.is_open();
                    std::ofstream("marker") << "created";
                }
                """;
        var result = runner.run(2, source, List.of("", ""));
        assertThat(result.cases()).extracting(DockerCodeRunner.CaseResult::output).containsExactly("0 1 0", "0 1 0");
    }

    @Test
    void cleansUpDaemonizedChildProcesses() {
        properties.setRunTimeout(Duration.ofMillis(300));
        var result = runner.run(2, "#include <unistd.h>\nint main() { if (fork() == 0) { setsid(); for (;;) {} } return 0; }", List.of(""));
        assertThat(result.cases().get(0).status()).isEqualTo("TIME_LIMIT");
    }

    @Test
    void keepsConcurrentSubmissionsWithSameMainFilenameSeparate() throws Exception {
        var first = CompletableFuture.supplyAsync(() -> runner.run(2, "#include <iostream>\nint main() { std::cout << 11; }", List.of("")));
        var second = CompletableFuture.supplyAsync(() -> runner.run(2, "#include <iostream>\nint main() { std::cout << 22; }", List.of("")));
        assertThat(first.get(30, TimeUnit.SECONDS).cases().get(0).output()).isEqualTo("11");
        assertThat(second.get(30, TimeUnit.SECONDS).cases().get(0).output()).isEqualTo("22");
    }

    @Test
    void missingImageIsAnInfrastructureErrorRatherThanAWrongAnswer() {
        properties.setImage("ses-judge:missing-" + UUID.randomUUID());
        assertThatThrownBy(() -> runner.run(2, "int main() {}", List.of("")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Docker");
    }

    @Test
    void exposesNoExternalNetworkAndCannotReadSupervisorInput() {
        String source = """
                #include <ifaddrs.h>
                #include <net/if.h>
                #include <sys/socket.h>
                #include <string>
                #include <fstream>
                #include <iostream>
                int main() {
                    ifaddrs* interfaces; getifaddrs(&interfaces); int external = 0;
                    // Kernels may expose dormant tunnel interfaces without addresses.
                    for (auto i = interfaces; i; i = i->ifa_next)
                        if (i->ifa_addr && (i->ifa_flags & IFF_UP) && std::string(i->ifa_name) != "lo"
                            && (i->ifa_addr->sa_family == AF_INET || i->ifa_addr->sa_family == AF_INET6)) external++;
                    freeifaddrs(interfaces);
                    std::ifstream supervisor("/proc/1/fd/0");
                    std::cout << external << " " << supervisor.is_open();
                }
                """;
        var result = runner.run(2, source, List.of("private input"));
        assertThat(result.cases().get(0).output()).isEqualTo("0 0");
    }
}
