package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiServiceTest {
    private static final String FIXTURE_KEY = "local-test-placeholder";

    private AiService service(HttpClient client) {
        AiService service = new AiService(mock(MemberRepository.class), mock(TrainerRepository.class),
                new ObjectMapper(), FIXTURE_KEY, "gemini-3.1-flash-lite");
        ReflectionTestUtils.setField(service, "httpClient", client);
        return service;
    }

    private User user() {
        User user = mock(User.class);
        when(user.getRole()).thenReturn("OWNER");
        when(user.getDisplayName()).thenReturn("Test owner");
        return user;
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (name, value) -> true));
        when(response.body()).thenReturn(body);
        return response;
    }

    @Test
    void recoversWhenFirstRequestIsOverloaded() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> unavailable = response(503, "provider unavailable");
        HttpResponse<String> success = response(200, """
                {"candidates":[{"content":{"parts":[{"text":"Keep a consistent training routine."}]}}]}
                """);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(unavailable, success);

        assertEquals("Keep a consistent training routine.", service(client).chat(user(), "How can I train consistently?"));
        verify(client, times(2)).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void reportsPersistentOverloadWithoutLeakingProviderBody() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> unavailable = response(503, "PRIVATE_PROVIDER_BODY " + FIXTURE_KEY);
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(unavailable);

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> service(client).chat(user(), "Suggest a warmup."));
        assertTrue(failure.getMessage().contains("HTTP 503"));
        assertFalse(failure.getMessage().contains(FIXTURE_KEY));
        assertFalse(failure.getMessage().contains("PRIVATE_PROVIDER_BODY"));
        verify(client, times(3)).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void doesNotRetryAuthenticationFailures() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> unauthorized = response(401, "unauthenticated");
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(unauthorized);

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> service(client).chat(user(), "Suggest a warmup."));
        assertTrue(failure.getMessage().contains("HTTP 401"));
        verify(client).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }
}
