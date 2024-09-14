package com.blog.entity;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WebSocketInfo {
    private String type;
    private String articleId;
    private String userId;
    private String message;
}
