package com.meetassistant.service;

import com.meetassistant.model.Meeting;
import com.meetassistant.model.MeetingSummary;
import com.meetassistant.model.Participant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender mailSender;
  private final String from;

  public EmailService(JavaMailSender mailSender, @Value("${app.email.from}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  public void sendSummary(Meeting meeting, MeetingSummary summary) {
    String subject = "Meeting summary: " + meeting.getTitle();
    String actionItems = summary.getActionItems() == null ? "None"
        : summary.getActionItems().stream()
            .map(item -> "- " + item.getDescription()
                + (item.getOwner() == null ? "" : " (" + item.getOwner() + ")"))
            .collect(Collectors.joining("\n"));

    String body = "Summary:\n" + summary.getSummaryText()
        + "\n\nAction items:\n" + actionItems
        + "\n\nAnalysis:\n" + String.valueOf(summary.getAnalysis());

    for (Participant participant : meeting.getParticipants()) {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(participant.getEmail());
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
    }
  }
}
