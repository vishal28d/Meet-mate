package com.meetassistant.controller;

import com.meetassistant.dto.CreateMeetingRequest;
import com.meetassistant.dto.JoinMeetingRequest;
import com.meetassistant.dto.TranscriptRequest;
import com.meetassistant.model.Meeting;
import com.meetassistant.service.MeetingService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
  private final MeetingService meetingService;

  public MeetingController(MeetingService meetingService) {
    this.meetingService = meetingService;
  }

  @PostMapping
  public Meeting createMeeting(@Valid @RequestBody CreateMeetingRequest request) {
    return meetingService.createMeeting(request.getTitle());
  }

  @PostMapping("/{meetingId}/join")
  public ResponseEntity<Void> joinMeeting(
      @PathVariable String meetingId, @Valid @RequestBody JoinMeetingRequest request) {
    meetingService.joinMeeting(meetingId, request.getEmail(), request.getDisplayName());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{meetingId}/transcripts")
  public ResponseEntity<Map<String, String>> addTranscript(
      @PathVariable String meetingId, @Valid @RequestBody TranscriptRequest request) {
    meetingService.addTranscript(meetingId, request);
    return ResponseEntity.ok(Map.of("status", "accepted"));
  }

  @PostMapping("/{meetingId}/end")
  public ResponseEntity<Map<String, String>> endMeeting(@PathVariable String meetingId) {
    meetingService.endMeeting(meetingId);
    return ResponseEntity.ok(Map.of("status", "ended"));
  }
}
