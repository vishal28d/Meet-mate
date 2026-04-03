package com.meetassistant.repo;

import com.meetassistant.model.TranscriptChunk;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TranscriptRepository extends MongoRepository<TranscriptChunk, String> {
  List<TranscriptChunk> findByMeetingIdOrderByCreatedAtAsc(String meetingId);
}
