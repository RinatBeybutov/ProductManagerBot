package com.customBot.v1.repository;

import com.customBot.v1.entity.CheckEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CheckRepository extends JpaRepository<CheckEntity, Long> {

  @Query("select c from CheckEntity c where c.chatId=:chatId")
  List<CheckEntity> findAllByChatId(Long chatId);

  @Query("select c.date from CheckEntity c where c.chatId=:chatId")
  List<Date> getCheckDates(Long chatId);

  @Query("select c from CheckEntity c where c.date = :date and c.chatId = :chatId")
  CheckEntity findByChatIdAndDate(Long chatId, Date date);

  @Query("delete from CheckEntity c where c.date = :date and c.chatId = :chatId")
  @Modifying
  void deleteByChatIdAndDate(Long chatId, Date date);
}
