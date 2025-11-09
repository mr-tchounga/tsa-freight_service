package com.shifter.freight_service.repositories;

import com.shifter.freight_service.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("select count(m) from ChatMessage m where m.request.id = :requestId and (m.senderId = :userId or m.receiverId = :userId)")
    long countByRequestIdAndUserId(@Param("requestId") Long requestId,
                                   @Param("userId") Long userId);

    @Query("select m from ChatMessage m where m.request.id = :requestId and (m.senderId = :userId or m.receiverId = :userId) order by m.createdAt asc")
    List<ChatMessage> findAllForRequestAndUser(@Param("requestId") Long requestId,
                                               @Param("userId") Long userId);
}
