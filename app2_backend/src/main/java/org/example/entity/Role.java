package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Role {
    private String name;  // e.g., "ROLE_USER", "ROLE_ADMIN"
}
