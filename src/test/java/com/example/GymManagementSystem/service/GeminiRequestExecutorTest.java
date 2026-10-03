package com.example.GymManagementSystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GeminiRequestExecutorTest {
    private HttpClient client;
    private HttpRequest request;
    private AtomicLong now;
    private List<Long> delays;
    private GeminiRequestExecutor.Sleeper sleeper;

    @BeforeEach
    void setUp() {
        client = mock(HttpClient.class);
        request = HttpRequest.newBuilder(URI.create("https://generativelanguage.googleapis.com/v1beta/models/test:generateContent"))
                .header("x-goog-api-key", "local-test-placeholder")
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        now = new AtomicLong();
        delays = new ArrayList<>();
        sleeper = millis -> {
            delays.add(millis);
            now.addAndGet(TimeUnit.MILLISECONDS.toNanos(millis));
        };
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String retryAfter) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        Map<String, List<String>> headers = retryAfter == null ? Map.of() : Map.of("Retry-After", List.of(retryAfter));
        when(response.headers()).thenReturn(HttpHeaders.of(headers, (name, value) -> true));
        return response;
    }

    private HttpResponse<String> send() throws Exception {
        return GeminiRequestExecutor.send(client, request, sleeper, now::get);
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    void recoversTransientFailuresAndPreservesRequest(int status) throws Exception {
        HttpResponse<String> unavailable = response(status, null);
        HttpResponse<String> success = response(200, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(unavailable, success);
        assertSame(success, send());
        assertEquals(1, delays.size());
        assertTrue(delays.get(0) >= 1_000 && delays.get(0) <= 1_250);

        ArgumentCaptor<HttpRequest> requests = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client, times(2)).send(requests.capture(), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        for (HttpRequest sent : requests.getAllValues()) {
            assertEquals(request.uri(), sent.uri());
            assertEquals(request.headers(), sent.headers());
            assertEquals("POST", sent.method());
            assertSame(request.bodyPublisher().orElseThrow(), sent.bodyPublisher().orElseThrow());
        }
        assertEquals(Duration.ofSeconds(45), requests.getAllValues().get(0).timeout().orElseThrow());
        assertEquals(Duration.ofSeconds(45).minusMillis(delays.get(0)), requests.getValue().timeout().orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 400, 401, 403, 404, 429, 501})
    void doesNotRetrySuccessOrNonTransientFailures(int status) throws Exception {
        HttpResponse<String> response = response(status, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);
        assertSame(response, send());
        assertTrue(delays.isEmpty());
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void stopsAfterThreeAttemptsWithIncreasingBackoff() throws Exception {
        HttpResponse<String> overloaded = response(503, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(overloaded);
        assertSame(overloaded, send());
        assertEquals(2, delays.size());
        assertTrue(delays.get(0) >= 1_000 && delays.get(0) <= 1_250);
        assertTrue(delays.get(1) >= 2_000 && delays.get(1) <= 2_250);
        verify(client, times(3)).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void respectsShortRetryAfterHeader() throws Exception {
        HttpResponse<String> overloaded = response(503, "3");
        HttpResponse<String> success = response(200, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(overloaded, success);
        assertSame(success, send());
        assertEquals(List.of(3_000L), delays);
    }

    @ParameterizedTest
    @ValueSource(strings = {"120", "999999999999999999999999", "Thu, 01 Jan 2099 00:00:00 GMT"})
    void doesNotIgnoreLongRetryAfterOrWaitIndefinitely(String retryAfter) throws Exception {
        HttpResponse<String> overloaded = response(503, retryAfter);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(overloaded);
        assertSame(overloaded, send());
        assertTrue(delays.isEmpty());
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void malformedRetryAfterUsesLocalBackoff() throws Exception {
        HttpResponse<String> overloaded = response(503, "not-a-date");
        HttpResponse<String> success = response(200, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(overloaded, success);
        assertSame(success, send());
        assertEquals(1, delays.size());
    }

    @Test
    void stopsWhenAnotherDelayWouldExhaustTimeBudget() throws Exception {
        HttpResponse<String> overloaded = response(503, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenAnswer(invocation -> {
                    now.set(TimeUnit.SECONDS.toNanos(44));
                    return overloaded;
                });
        assertSame(overloaded, send());
        assertTrue(delays.isEmpty());
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void rechecksDeadlineAfterSleeping() throws Exception {
        HttpResponse<String> overloaded = response(503, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(overloaded);
        assertSame(overloaded, GeminiRequestExecutor.send(client, request,
                millis -> now.set(TimeUnit.SECONDS.toNanos(46)), now::get));
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void stopsImmediatelyWhenRetrySleepIsInterrupted() throws Exception {
        HttpResponse<String> overloaded = response(503, null);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(overloaded);
        assertThrows(InterruptedException.class, () -> GeminiRequestExecutor.send(client, request,
                millis -> { throw new InterruptedException("test cancellation"); }, now::get));
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void doesNotReplayAmbiguousNetworkFailures() throws Exception {
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("test connection failure"));
        assertThrows(IOException.class, this::send);
        assertTrue(delays.isEmpty());
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }
}
