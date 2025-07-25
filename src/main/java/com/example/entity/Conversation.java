package com.example.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Conversation {
    private Long id;
    private Long userId;
    private String title;
    private Date createdAt;
    private Date updatedAt;
}