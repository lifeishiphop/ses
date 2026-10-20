package com.web.back.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.back.config.JudgeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
public class DockerCodeRunner {
    public static final int MAX_SOURCE_BYTES = 65_536;
    public static final int MAX_INPUT_BYTES = 65_536;
    public static final int MAX_OUTPUT_BYTES = 65_536;
    public static final int MAX_TESTCASES = 20;

    public record CaseResult(String status, String output, String error) {}
    public record Result(String status, List<CaseResult> cases) {}
    private record Request(int language, String source, List<String> inputs,
                           long compileTimeoutMs, long runTimeoutMs, int outputLimitBytes) {}

    private final JudgeProperties properties;
    private final ObjectMapper mapper;
    private final Semaphore slots;

    public DockerCodeRunner(JudgeProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
        this.slots = new Semaphore(properties.getMaxConcurrent());
    }

    public Result run(int language, String source, List<String> inputs) {
        if (language != 1 && language != 2) throw new IllegalArgumentException("지원되지 않는 언어입니다.");
        if (source == null || source.isBlank() || source.getBytes(StandardCharsets.UTF_8).length > MAX_SOURCE_BYTES) {
            throw new IllegalArgumentException("소스 코드는 비어 있지 않은 UTF-8 64 KiB 이하여야 합니다.");
        }
        if (inputs == null || inputs.isEmpty() || inputs.size() > MAX_TESTCASES) {
            throw new IllegalArgumentException("테스트케이스는 1~20개여야 합니다.");
        }
        for (String input : inputs) {
            if (input == null || input.getBytes(StandardCharsets.UTF_8).length > MAX_INPUT_BYTES) {
                throw new IllegalArgumentException("테스트 입력은 UTF-8 64 KiB 이하여야 합니다.");
            }
        }
        if (!slots.tryAcquire()) throw new IllegalStateException("채점 요청이 많습니다. 잠시 후 다시 시도해주세요.");
        String name = "ses-judge-" + UUID.randomUUID();
        ExecutorService io = Executors.newFixedThreadPool(3);
        Process process = null;
        boolean interrupted = false;
        try {
            long compileMs = properties.getCompileTimeout().toMillis();
            long runMs = properties.getRunTimeout().toMillis();
            byte[] request = mapper.writeValueAsBytes(new Request(language, source, inputs, compileMs, runMs, MAX_OUTPUT_BYTES));
            process = new ProcessBuilder(command(name)).start();
            Process running = process;
            Future<?> writer = io.submit(() -> {
                try (var stdin = running.getOutputStream()) { stdin.write(request); }
                catch (IOException e) { throw new java.io.UncheckedIOException(e); }
            });
            int responseLimit = MAX_OUTPUT_BYTES * inputs.size() * 6 + 4096;
            Future<byte[]> stdout = io.submit(() -> readLimited(running.getInputStream(), responseLimit));
            Future<byte[]> stderr = io.submit(() -> readLimited(running.getErrorStream(), MAX_OUTPUT_BYTES));
            long wallMs = Math.addExact(15_000L, Math.addExact(compileMs, Math.multiplyExact(runMs, inputs.size())));
            if (!process.waitFor(wallMs, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("채점기 전체 실행 시간이 초과되었습니다.");
            }
            String error = new String(stderr.get(2, TimeUnit.SECONDS), StandardCharsets.UTF_8);
            if (process.exitValue() == 137) return new Result("MEMORY_LIMIT", List.of());
            if (process.exitValue() != 0) {
                log.warn("Docker grader failed (exit {}): {}", process.exitValue(), error);
                throw new IllegalStateException("Docker 채점기를 실행할 수 없습니다. Docker와 채점 이미지 설정을 확인해주세요.");
            }
            writer.get(2, TimeUnit.SECONDS);
            Result result = mapper.readValue(stdout.get(2, TimeUnit.SECONDS), Result.class);
            if (result.status() == null || result.cases() == null) throw new IOException("채점 응답 형식 오류");
            return result;
        } catch (InterruptedException e) {
            interrupted = true;
            throw new IllegalStateException("채점이 중단되었습니다.", e);
        } catch (IOException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Docker 채점 응답을 처리할 수 없습니다. Docker와 채점 이미지 설정을 확인해주세요.", e);
        } finally {
            try {
                removeContainer(name);
            } finally {
                if (process != null && process.isAlive()) process.destroyForcibly();
                io.shutdownNow();
                slots.release();
                if (interrupted) Thread.currentThread().interrupt();
            }
        }
    }

    private List<String> command(String name) {
        return List.of(properties.getDockerCommand(), "run", "--rm", "--pull=never", "--interactive", "--init",
                "--name", name, "--label", "ses.judge=true", "--network=none", "--read-only",
                "--cpus=1", "--memory", properties.getMemory(), "--memory-swap", properties.getMemory(),
                "--pids-limit=128", "--cap-drop=ALL", "--cap-add=SETUID", "--cap-add=SETGID",
                "--cap-add=KILL", "--cap-add=CHOWN", "--security-opt=no-new-privileges", "--log-driver=none",
                "--ulimit", "nofile=128:128", "--tmpfs", "/tmp:rw,nosuid,nodev,noexec,size=32m,mode=1777",
                "--tmpfs", "/workspace:rw,nosuid,nodev,exec,size=64m,mode=755", properties.getImage());
    }

    private static byte[] readLimited(InputStream input, int limit) throws IOException {
        try (input) {
            byte[] data = input.readNBytes(limit + 1);
            if (data.length > limit) throw new IOException("채점 응답 크기 제한 초과");
            return data;
        }
    }

    private void removeContainer(String name) {
        Process cleanup = null;
        try {
            cleanup = new ProcessBuilder(properties.getDockerCommand(), "rm", "--force", name)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            if (!cleanup.waitFor(5, TimeUnit.SECONDS)) log.warn("Grader container cleanup timed out: {}", name);
        } catch (IOException e) {
            log.warn("Cannot clean grader container {}: {}", name, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Grader container cleanup interrupted: {}", name);
        } finally {
            if (cleanup != null && cleanup.isAlive()) cleanup.destroyForcibly();
        }
    }
}
