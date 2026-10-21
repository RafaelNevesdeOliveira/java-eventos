package br.com.fiap.b3.aula2.application.port.out;

import java.util.UUID;

public interface InboxRepository {
  boolean register(String consumer, String source, UUID eventId);
}
