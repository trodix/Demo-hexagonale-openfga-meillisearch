package com.trodix.demo.adapter.in.dto;

public record EntityPermissionStatus(
    String entityId,
    String entityName,
    PermissionStatus read,
    PermissionStatus write,
    PermissionStatus delete
) {}
