package org.lxdproject.lxd.global.discord;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscordAlarmService {

    private final WebClient webClient;

    private static final String APP_PKG_PREFIX = "org.lxdproject.lxd.";
    private static final int APP_FRAMES = 12;
    private static final int OTHER_FRAMES = 5;
    private static final int MAX_EMBED_DESC = 1800;
    private static final String TRUNC_SUFFIX = "\n... (truncated)";

    @Value("${discord.enabled:false}")
    private boolean enabled;

    @Value("${discord.error-alert-url:}")
    private String errorWebhookUrl;

    public void sendErrorAlert(Throwable e, String method, String path, String query) {
        if (!enabled || errorWebhookUrl == null || errorWebhookUrl.isBlank()) return;

        String title = "🚨 예외 발생";
        String desc  = buildDescription(e, method, path, query);

        DiscordWebhookPayload payload = new DiscordWebhookPayload(
                java.util.List.of(new DiscordWebhookPayload.Embed(title, desc))
        );

        webClient.post()
                .uri(errorWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(reactor.util.retry.Retry
                        .fixedDelay(3, java.time.Duration.ofSeconds(2))
                        .filter(ex -> ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests))
                .onErrorResume(ex -> reactor.core.publisher.Mono.empty())
                .subscribe();
    }

    private String buildDescription(Throwable e, String method, String path, String query) {
        StringBuilder header = new StringBuilder();

        if (method != null || path != null) {
            header.append("• Endpoint: `").append(nullToEmpty(method)).append(" ").append(nullToEmpty(path)).append("`\n");
        }
        if (query != null && !query.isBlank()) {
            header.append("• Query: `").append(safe(query)).append("`\n");
        }
        header.append("• Type: `").append(e.getClass().getName()).append("`\n");
        header.append("• Message: `").append(safe(e.getMessage())).append("`\n\n");

        String stack = formatStack(rootCause(e));

        int overhead = header.length() + 6;
        int budget = Math.max(0, MAX_EMBED_DESC - overhead);
        boolean truncated = false;

        if (stack.length() > budget) {
            int sliceBudget = Math.max(0, budget - TRUNC_SUFFIX.length());
            stack = stack.substring(0, Math.max(0, sliceBudget));
            truncated = true;
        }

        StringBuilder desc = new StringBuilder(header)
                .append("```").append(stack).append("```");
        if (truncated) desc.append(TRUNC_SUFFIX);

        return desc.toString();
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }

    private String formatStack(Throwable root) {
        StackTraceElement[] frames = root.getStackTrace();

        List<StackTraceElement> app = new ArrayList<>();
        List<StackTraceElement> others = new ArrayList<>();

        for (StackTraceElement f : frames) {
            if (f.getClassName().startsWith(APP_PKG_PREFIX)) {
                if (app.size() < APP_FRAMES) app.add(f);
            } else {
                if (others.size() < OTHER_FRAMES) others.add(f);
            }
            if (app.size() >= APP_FRAMES && others.size() >= OTHER_FRAMES) break;
        }

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement f : app) sb.append("at ").append(f).append("\n");
        if (!others.isEmpty()) {
            sb.append("--- non-app frames ---\n");
            for (StackTraceElement f : others) sb.append("at ").append(f).append("\n");
        }

        int shown = app.size() + others.size();
        int remaining = Math.max(0, frames.length - shown);
        if (remaining > 0) sb.append("... (+").append(remaining).append(" more)");
        return sb.toString();
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
