package com.google.a2a.client.core;

import com.google.a2a.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit tests for A2AClient
 */
public class SimpleA2AClientTest {
    
    @Test
    public void testClientCreation() {
        A2AClient client = new A2AClient("http://localhost:8080");
        assertNotNull(client);
    }
    
    @Test
    public void testExceptionCreation() {
        A2AClientException exception = new A2AClientException("Test message");
        assertEquals("Test message", exception.getMessage());
        assertNull(exception.getErrorCode());
        
        A2AClientException exceptionWithCode = new A2AClientException("Test message", 123);
        assertEquals("Test message", exceptionWithCode.getMessage());
        assertEquals(Integer.valueOf(123), exceptionWithCode.getErrorCode());
    }
    
    @Test
    public void testModelClasses() {
        // Test that we can create basic model objects
        TextPart textPart = new TextPart("text", "Hello, world!", null);
        
        assertNotNull(textPart);
        assertEquals("text", textPart.kind());
        assertEquals("Hello, world!", textPart.text());
        
        // Test a simple AgentSkill creation
        AgentSkill skill = new AgentSkill(
            "test-skill",
            "Test Skill",
            "A test skill",
            List.of("test"),
            List.of("test example"),
            null,
            null
        );
        
        assertNotNull(skill);
        assertEquals("test-skill", skill.id());
        assertEquals("Test Skill", skill.name());
        assertEquals("A test skill", skill.description());
        assertEquals(List.of("test"), skill.tags());
        assertEquals(List.of("test example"), skill.examples());
    }
} 