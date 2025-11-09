package com.shifter.freight_service.payloads.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long requestId;
    private Long senderId;
    private Long receiverId;
    private String message;
    private Date createdAt;
}
