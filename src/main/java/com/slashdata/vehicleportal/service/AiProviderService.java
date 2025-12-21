package com.slashdata.vehicleportal.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.slashdata.vehicleportal.dto.AiConnectionResult;
import com.slashdata.vehicleportal.entity.AiProvider;
import com.slashdata.vehicleportal.entity.AppConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class AiProviderService {

    private static final String GEMINI_MODEL = "gemini-1.5-flash";
    private static final String OPENAI_MODEL = "gpt-4o-mini";
    private static final String OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String TEST_PROMPT = "Respond with 'pong'.";

    private final AppConfigService appConfigService;
    private final Environment environment;
    private final RestTemplate restTemplate;

    public AiProviderService(AppConfigService appConfigService, Environment environment, RestTemplate restTemplate) {
        this.appConfigService = appConfigService;
        this.environment = environment;
        this.restTemplate = restTemplate;
    }

    public AiConnectionResult testConnection() {
        long start = System.currentTimeMillis();
        generateContent(TEST_PROMPT);
        long latency = System.currentTimeMillis() - start;
        return new AiConnectionResult("success", latency);
    }

    public String generateContent(String prompt) {
        AppConfig config = appConfigService.getConfig();
        AiProvider provider = config.getAiProvider() != null ? config.getAiProvider() : AiProvider.GEMINI;
        String systemInstruction = config.getSystemInstruction();
        String apiKey = resolveApiKey(provider);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI API key is not configured");
        }
        if (provider == AiProvider.OPENAI) {
            return callOpenAi(apiKey, systemInstruction, prompt);
        }
        return callGemini(apiKey, systemInstruction, prompt);
    }

    private String callGemini(String apiKey, String systemInstruction, String prompt) {
        try {
            Client client = new Client(apiKey);
            GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();
            if (systemInstruction != null && !systemInstruction.isBlank()) {
                Content systemContent = Content.builder()
                    .parts(List.of(Part.fromText(systemInstruction)))
                    .build();
                configBuilder.systemInstruction(systemContent);
            }
            GenerateContentResponse response = client.models.generateContent(
                GEMINI_MODEL,
                prompt,
                configBuilder.build()
            );
            return response.text();
        } catch (Exception ex) {
            throw new IllegalStateException("Gemini request failed: " + ex.getMessage(), ex);
        }
    }

    private String callOpenAi(String apiKey, String systemInstruction, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemInstruction));
        }
        messages.add(Map.of("role", "user", "content", prompt));
        Map<String, Object> requestBody = Map.of(
            "model", OPENAI_MODEL,
            "messages", messages,
            "max_tokens", 10
        );
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                OPENAI_CHAT_COMPLETIONS_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
            );
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("choices")) {
                throw new IllegalStateException("OpenAI response missing choices");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices.isEmpty()) {
                throw new IllegalStateException("OpenAI response contains no choices");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null || !message.containsKey("content")) {
                throw new IllegalStateException("OpenAI response missing message content");
            }
            return String.valueOf(message.get("content"));
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            throw new IllegalStateException("OpenAI request failed: " + responseBody, ex);
        }
    }

    private String resolveApiKey(AiProvider provider) {
        String decrypted = appConfigService.getDecryptedAiApiKey();
        if (decrypted != null && !decrypted.isBlank()) {
            return decrypted;
        }
        if (provider == AiProvider.OPENAI) {
            return environment.getProperty("OPENAI_API_KEY");
        }
        return environment.getProperty("GEMINI_API_KEY");
    }
}
