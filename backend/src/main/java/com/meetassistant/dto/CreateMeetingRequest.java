package com.meetassistant.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateMeetingRequest {
  @NotBlank
  private String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
