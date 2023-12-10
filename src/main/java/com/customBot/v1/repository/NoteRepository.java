package com.customBot.v1.repository;

import com.customBot.v1.entity.NoteEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {

  void deleteAllByChatId(Long chatId);

  @Query("select n.text from NoteEntity n where n.chatId = :chatId")
  Optional<String> findByChatId(Long chatId);
}
