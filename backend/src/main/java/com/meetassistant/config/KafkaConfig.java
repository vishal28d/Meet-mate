package com.meetassistant.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
  @Bean
  public NewTopic meetingEndedTopic(
      @Value("${app.kafka.topicMeetingEnded}") String topicName) {
    return new NewTopic(topicName, 1, (short) 1);
  }

  @Bean
  public NewTopic summaryReadyTopic(
      @Value("${app.kafka.topicSummaryReady}") String topicName) {
    return new NewTopic(topicName, 1, (short) 1);
  }
}
