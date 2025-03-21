package com.mahmud.customexceptionrestapi.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class User {
    private int id;
    private String name;
    private boolean admin;
}
