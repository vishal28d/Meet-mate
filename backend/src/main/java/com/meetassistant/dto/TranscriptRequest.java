package com.meetassistant.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class TranscriptRequest {
  @NotBlank
  private String speaker;

  @NotBlank
  private String text;

  private Instant createdAt;

  public String getSpeaker() {
    return speaker;
  }

  public void setSpeaker(String speaker) {
    this.speaker = speaker;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
