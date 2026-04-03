package com.meetassistant.worker;

import com.meetassistant.model.MeetingSummary;
import com.meetassistant.repo.MeetingRepository;
import com.meetassistant.service.SummaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SummaryWorker {
  private final SummaryService summaryService;
  private final MeetingRepository meetingRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final String summaryReadyTopic;

  public SummaryWorker(
      SummaryService summaryService,
      MeetingRepository meetingRepository,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${app.kafka.topicSummaryReady}") String summaryReadyTopic) {
    this.summaryService = summaryService;
    this.meetingRepository = meetingRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.summaryReadyTopic = summaryReadyTopic;
  }

  @KafkaListener(topics = "${app.kafka.topicMeetingEnded}")
  public void handleMeetingEnded(String meetingId) {
    if (meetingRepository.findById(meetingId).isEmpty()) {
      return;
    }
    MeetingSummary summary = summaryService.generateSummary(meetingId);
    kafkaTemplate.send(summaryReadyTopic, summary.getMeetingId());
  }
}
