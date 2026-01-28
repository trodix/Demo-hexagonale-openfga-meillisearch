package com.trodix.demo.adapter.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsResponse {
    private String username;
    private String tenantId;
    private List<PermissionTuple> permissions;
}
