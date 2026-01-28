package com.trodix.demo.adapter.in.dto;

public record ProductPermissionStatus(
    String productId,
    PermissionStatus read,
    PermissionStatus write,
    PermissionStatus delete
) {}
