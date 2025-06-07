package com.google.a2a.client.core;

import com.google.a2a.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for A2AClient
 */
@ExtendWith(MockitoExtension.class)
class A2AClientTest {
    
    @Mock
    private HttpClient mockHttpClient;
    
    @Mock
    private HttpResponse<String> mockResponse;
    
    private A2AClient client;
    
    @BeforeEach
    void setUp() {
        client = new A2AClient("http://localhost:8080", mockHttpClient, Map.of());
    }
    
    @Test
    void testClientInstantiation() {
        // Test basic client instantiation
        assertNotNull(client);
        
        // Test different constructors
        A2AClient basicClient = new A2AClient("http://localhost:8080");
        assertNotNull(basicClient);
        
        A2AClient clientWithHeaders = new A2AClient("http://localhost:8080", 
            HttpClient.newHttpClient(), Map.of("Authorization", "Bearer test"));
        assertNotNull(clientWithHeaders);
    }
    
    @Test
    void testGetAgentCard() throws Exception {
        // Mock successful response
        String responseBody = """
            {
                "name": "Test Agent",
                "description": "A test agent for unit testing",
                "url": "http://localhost:8080",
                "version": "1.0.0",
                "capabilities": {
                    "streaming": true,
                    "pushNotifications": false,
                    "stateTransitionHistory": false,
                    "extensions": []
                },
                "defaultInputModes": ["application/json", "text/plain"],
                "defaultOutputModes": ["application/json", "text/plain"],
                "skills": [
                    {
                        "id": "test-skill",
                        "name": "Test Skill",
                        "description": "A test skill",
                        "tags": ["test"],
                        "examples": ["test example"]
                    }
                ]
            }
            """;
        
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(responseBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
        
        // Execute test
        AgentCard agentCard = client.getAgentCard();
        
        // Verify results
        assertNotNull(agentCard);
        assertEquals("Test Agent", agentCard.name());
        assertEquals("http://localhost:8080", agentCard.url());
        assertTrue(agentCard.capabilities().streaming());
    }
    
    @Test
    void testHttpError() throws Exception {
        // Mock HTTP error response
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
        
        // Execute test and expect exception
        A2AClientException exception = assertThrows(A2AClientException.class, () -> {
            client.getAgentCard();
        });
        
        assertTrue(exception.getMessage().contains("500"));
    }
    
    @Test
    void testGetAuthenticatedExtendedCard() throws Exception {
        // Mock successful response
        String responseBody = """
            {
                "name": "Test Agent Extended",
                "description": "Extended test agent",
                "url": "http://localhost:8080",
                "version": "1.0.0",
                "capabilities": {
                    "streaming": true,
                    "pushNotifications": true,
                    "stateTransitionHistory": true,
                    "extensions": []
                },
                "defaultInputModes": ["application/json", "text/plain"],
                "defaultOutputModes": ["application/json", "text/plain"],
                "skills": []
            }
            """;
        
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(responseBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);
        
        // Execute test
        AgentCard extendedCard = client.getAuthenticatedExtendedCard();
        
        // Verify results
        assertNotNull(extendedCard);
        assertEquals("Test Agent Extended", extendedCard.name());
        assertTrue(extendedCard.capabilities().pushNotifications());
    }
    
    @Test
    void testStreamingEventListener() {
        // Test the streaming event listener interface
        CountDownLatch completeLatch = new CountDownLatch(1);
        MockStreamingEventListener listener = new MockStreamingEventListener(completeLatch);
        
        // Test event handling
        listener.onEvent("test event");
        assertEquals(1, listener.getEventCount());
        
        // Test error handling
        Exception testException = new RuntimeException("test error");
        listener.onError(testException);
        assertEquals(testException, listener.getError());
        
        // Test completion
        listener.onComplete();
        assertTrue(listener.isCompleted());
    }
    
    private static class MockStreamingEventListener implements StreamingEventListener {
        
        private final CountDownLatch completeLatch;
        private int eventCount = 0;
        private boolean completed = false;
        private Exception error = null;
        
        public MockStreamingEventListener(CountDownLatch completeLatch) {
            this.completeLatch = completeLatch;
        }
        
        @Override
        public void onEvent(Object event) {
            eventCount++;
        }
        
        @Override
        public void onError(Exception exception) {
            this.error = exception;
            completeLatch.countDown();
        }
        
        @Override
        public void onComplete() {
            this.completed = true;
            completeLatch.countDown();
        }
        
        public int getEventCount() {
            return eventCount;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public Exception getError() {
            return error;
        }
    }
} 