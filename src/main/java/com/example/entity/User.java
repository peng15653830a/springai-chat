package com.example.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String nickname;
    private Date createdAt;
    private Date updatedAt;
}