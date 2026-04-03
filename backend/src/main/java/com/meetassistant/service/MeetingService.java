package com.meetassistant.service;

import com.meetassistant.dto.TranscriptEvent;
import com.meetassistant.dto.TranscriptRequest;
import com.meetassistant.model.Meeting;
import com.meetassistant.model.MeetingStatus;
import com.meetassistant.model.Participant;
import com.meetassistant.model.TranscriptChunk;
import com.meetassistant.repo.MeetingRepository;
import com.meetassistant.repo.TranscriptRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MeetingService {
  private final MeetingRepository meetingRepository;
  private final TranscriptRepository transcriptRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final SimpMessagingTemplate messagingTemplate;
  private final String meetingEndedTopic;

  public MeetingService(
      MeetingRepository meetingRepository,
      TranscriptRepository transcriptRepository,
      KafkaTemplate<String, String> kafkaTemplate,
      @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
      SimpMessagingTemplate messagingTemplate,
      @Value("${app.kafka.topicMeetingEnded}") String meetingEndedTopic) {
    this.meetingRepository = meetingRepository;
    this.transcriptRepository = transcriptRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.redisTemplate = redisTemplate;
    this.messagingTemplate = messagingTemplate;
    this.meetingEndedTopic = meetingEndedTopic;
  }

  public Meeting createMeeting(String title) {
    Meeting meeting = new Meeting();
    meeting.setTitle(title);
    meeting.setStatus(MeetingStatus.ACTIVE);
    meeting.setStartedAt(Instant.now());
    Meeting saved = meetingRepository.save(meeting);
    cacheActiveMeeting(saved.getId());
    return saved;
  }

  public void joinMeeting(String meetingId, String email, String displayName) {
    Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
    Optional<Participant> existing = meeting.getParticipants().stream()
        .filter(p -> p.getEmail().equalsIgnoreCase(email))
        .findFirst();
    if (existing.isEmpty()) {
      meeting.getParticipants().add(new Participant(email, displayName));
      meetingRepository.save(meeting);
    }
    cacheActiveMeeting(meetingId);
  }

  public void addTranscript(String meetingId, TranscriptRequest request) {
    TranscriptChunk chunk = new TranscriptChunk();
    chunk.setMeetingId(meetingId);
    chunk.setSpeaker(request.getSpeaker());
    chunk.setText(request.getText());
    chunk.setCreatedAt(request.getCreatedAt() == null ? Instant.now() : request.getCreatedAt());
    TranscriptChunk saved = transcriptRepository.save(chunk);
    TranscriptEvent event = new TranscriptEvent();
    event.setId(saved.getId());
    event.setMeetingId(saved.getMeetingId());
    event.setSpeaker(saved.getSpeaker());
    event.setText(saved.getText());
    event.setCreatedAt(saved.getCreatedAt());
    messagingTemplate.convertAndSend(
      "/topic/meetings/" + meetingId + "/transcripts", event);
    cacheActiveMeeting(meetingId);
  }

  public void endMeeting(String meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
    meeting.setStatus(MeetingStatus.ENDED);
    meeting.setEndedAt(Instant.now());
    meetingRepository.save(meeting);
    kafkaTemplate.send(meetingEndedTopic, meetingId);
    redisTemplate.delete(activeKey(meetingId));
  }

  private void cacheActiveMeeting(String meetingId) {
    redisTemplate.opsForValue().set(activeKey(meetingId), "active", Duration.ofHours(6));
  }

  private String activeKey(String meetingId) {
    return "meeting:active:" + meetingId;
  }
}
