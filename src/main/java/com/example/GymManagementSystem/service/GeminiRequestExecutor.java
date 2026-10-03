package com.example.GymManagementSystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

/** Bounded recovery for provider-side failures, without changing credentials or models. */
final class GeminiRequestExecutor {
    private static final Logger log = LoggerFactory.getLogger(GeminiRequestExecutor.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration TOTAL_TIMEOUT = Duration.ofSeconds(45);
    private static final long MAX_RETRY_DELAY_MILLIS = 5_000;

    private GeminiRequestExecutor() { }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    static HttpResponse<String> send(HttpClient client, HttpRequest request)
            throws IOException, InterruptedException {
        return send(client, request, Thread::sleep, System::nanoTime);
    }

    // The clock and sleeper are injectable so tests never wait or contact Gemini.
    static HttpResponse<String> send(HttpClient client, HttpRequest request,
                                     Sleeper sleeper, LongSupplier nanoTime)
            throws IOException, InterruptedException {
        long startedAt = nanoTime.getAsLong();
        HttpResponse<String> response = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            long remainingNanos = TOTAL_TIMEOUT.toNanos() - (nanoTime.getAsLong() - startedAt);
            if (remainingNanos <= 0) {
                if (response != null) return response;
                throw new HttpTimeoutException("Gemini request time budget was exhausted.");
            }

            HttpRequest timedRequest = HttpRequest.newBuilder(request, (name, value) -> true)
                    .timeout(Duration.ofNanos(remainingNanos))
                    .build();
            response = client.send(timedRequest, HttpResponse.BodyHandlers.ofString());
            if (!isRetryable(response.statusCode()) || attempt == MAX_ATTEMPTS) return response;

            long delayMillis = retryDelayMillis(response, attempt);
            long remainingMillis = Duration.ofNanos(Math.max(0,
                    TOTAL_TIMEOUT.toNanos() - (nanoTime.getAsLong() - startedAt))).toMillis();
            if (delayMillis < 0 || delayMillis >= remainingMillis) return response;

            // Status/timing only: never log headers, account context, prompts, or provider bodies.
            log.warn("Gemini returned HTTP {}; retry {}/{} in {} ms",
                    response.statusCode(), attempt, MAX_ATTEMPTS - 1, delayMillis);
            sleeper.sleep(delayMillis);
        }
        return response;
    }

    private static boolean isRetryable(int status) {
        return status == 500 || status == 502 || status == 503 || status == 504;
    }

    private static long retryDelayMillis(HttpResponse<String> response, int attempt) {
        long backoff = (1_000L << (attempt - 1)) + ThreadLocalRandom.current().nextLong(251);
        String retryAfter = response.headers().firstValue("Retry-After").orElse("").trim();
        if (retryAfter.isEmpty()) return backoff;

        long requestedDelay;
        if (retryAfter.matches("[0-9]+")) {
            try {
                long seconds = Long.parseLong(retryAfter);
                if (seconds > MAX_RETRY_DELAY_MILLIS / 1_000) return -1;
                requestedDelay = seconds * 1_000;
            } catch (NumberFormatException exception) {
                return -1; // An enormous positive value cannot fit this request's time budget.
            }
        } else {
            try {
                Instant retryAt = ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                requestedDelay = Math.max(0, Duration.between(Instant.now(), retryAt).toMillis());
            } catch (DateTimeParseException exception) {
                return backoff; // Ignore invalid headers, but still bound local retries.
            }
        }
        // Do not retry early when the provider asks for a longer wait; let the user retry later.
        if (requestedDelay > MAX_RETRY_DELAY_MILLIS) return -1;
        return Math.max(backoff, requestedDelay);
    }
}
