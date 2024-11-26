package com.spring_cloud.eureka.client.auth.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String userId;
    private String username;
    private String password;
    private String role;
}
