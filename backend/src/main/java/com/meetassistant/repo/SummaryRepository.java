package com.meetassistant.repo;

import com.meetassistant.model.MeetingSummary;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SummaryRepository extends MongoRepository<MeetingSummary, String> {
  Optional<MeetingSummary> findByMeetingId(String meetingId);
}
