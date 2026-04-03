package com.meetassistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetassistant.model.MeetingSummary;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GeminiClient {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String model;

  public GeminiClient(
      WebClient webClient,
      ObjectMapper objectMapper,
      @Value("${app.gemini.apiKey}") String apiKey,
      @Value("${app.gemini.model}") String model) {
    this.webClient = webClient;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.model = model;
  }

  public MeetingSummary summarize(String transcript) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("GEMINI_API_KEY is not set");
    }
    String prompt = "You are an AI meeting assistant. Return JSON only with keys: "
        + "summaryText (string), actionItems (array of {description, owner, dueDate}), "
        + "analysis (object with keys like sentiment, topics, risks).";

    Map<String, Object> payload = new HashMap<>();
    payload.put("contents", new Object[] {
        Map.of("role", "user", "parts", new Object[] { Map.of("text", prompt) }),
        Map.of("role", "user", "parts", new Object[] { Map.of("text", transcript) })
    });
    payload.put("generationConfig", Map.of("responseMimeType", "application/json"));

    String response = webClient.post()
        .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model
            + ":generateContent?key=" + apiKey)
        .bodyValue(payload)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    if (response == null) {
      throw new IllegalStateException("Gemini response was empty");
    }

    try {
      JsonNode root = objectMapper.readTree(response);
      String jsonText = root.at("/candidates/0/content/parts/0/text").asText();
      JsonNode summaryNode = objectMapper.readTree(jsonText);
      MeetingSummary summary = objectMapper.treeToValue(summaryNode, MeetingSummary.class);
      summary.setCreatedAt(Instant.now());
      return summary;
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse Gemini response", ex);
    }
  }
}
