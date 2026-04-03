package com.meetassistant.worker;

import com.meetassistant.model.Meeting;
import com.meetassistant.model.MeetingSummary;
import com.meetassistant.repo.MeetingRepository;
import com.meetassistant.repo.SummaryRepository;
import com.meetassistant.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailWorker {
  private final MeetingRepository meetingRepository;
  private final SummaryRepository summaryRepository;
  private final EmailService emailService;

  public EmailWorker(
      MeetingRepository meetingRepository,
      SummaryRepository summaryRepository,
      EmailService emailService) {
    this.meetingRepository = meetingRepository;
    this.summaryRepository = summaryRepository;
    this.emailService = emailService;
  }

  @KafkaListener(topics = "${app.kafka.topicSummaryReady}")
  public void handleSummaryReady(String meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
    MeetingSummary summary = summaryRepository.findByMeetingId(meetingId).orElseThrow();
    emailService.sendSummary(meeting, summary);
  }
}
