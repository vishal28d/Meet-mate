package com.meetassistant.service;

import com.meetassistant.model.Meeting;
import com.meetassistant.model.MeetingSummary;
import com.meetassistant.model.TranscriptChunk;
import com.meetassistant.repo.MeetingRepository;
import com.meetassistant.repo.SummaryRepository;
import com.meetassistant.repo.TranscriptRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SummaryService {
  private final MeetingRepository meetingRepository;
  private final TranscriptRepository transcriptRepository;
  private final SummaryRepository summaryRepository;
  private final GeminiClient geminiClient;

  public SummaryService(
      MeetingRepository meetingRepository,
      TranscriptRepository transcriptRepository,
      SummaryRepository summaryRepository,
      GeminiClient geminiClient) {
    this.meetingRepository = meetingRepository;
    this.transcriptRepository = transcriptRepository;
    this.summaryRepository = summaryRepository;
    this.geminiClient = geminiClient;
  }

  public MeetingSummary generateSummary(String meetingId) {
    Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
    List<TranscriptChunk> chunks = transcriptRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId);
    String transcript = chunks.stream()
        .map(chunk -> chunk.getSpeaker() + ": " + chunk.getText())
        .collect(Collectors.joining("\n"));

    MeetingSummary summary = geminiClient.summarize(transcript);
    summary.setMeetingId(meeting.getId());
    return summaryRepository.save(summary);
  }
}
