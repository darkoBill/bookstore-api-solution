package com.bookstore.dto;

import java.util.UUID;

public interface NamedDto {
    UUID id();
    String name();
}