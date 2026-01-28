package com.trodix.demo.adapter.in.dto;

public record PermissionStatus(
    boolean hasPermission,
    boolean isDirect
) {}
