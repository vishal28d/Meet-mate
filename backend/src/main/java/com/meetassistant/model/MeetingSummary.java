package com.meetassistant.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("summaries")
public class MeetingSummary {
  @Id
  private String id;
  private String meetingId;
  private String summaryText;
  private List<ActionItem> actionItems;
  private Map<String, Object> analysis;
  private Instant createdAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMeetingId() {
    return meetingId;
  }

  public void setMeetingId(String meetingId) {
    this.meetingId = meetingId;
  }

  public String getSummaryText() {
    return summaryText;
  }

  public void setSummaryText(String summaryText) {
    this.summaryText = summaryText;
  }

  public List<ActionItem> getActionItems() {
    return actionItems;
  }

  public void setActionItems(List<ActionItem> actionItems) {
    this.actionItems = actionItems;
  }

  public Map<String, Object> getAnalysis() {
    return analysis;
  }

  public void setAnalysis(Map<String, Object> analysis) {
    this.analysis = analysis;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
