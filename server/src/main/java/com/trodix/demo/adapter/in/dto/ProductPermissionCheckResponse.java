package com.trodix.demo.adapter.in.dto;

public record ProductPermissionCheckResponse(
    String username,
    String productId,
    String relation,
    boolean allowed
) {}
