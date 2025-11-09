package com.shifter.freight_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Long requestId;
    private Long receiverId;
    private String message;
    private Long parentId;
}
