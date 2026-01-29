package com.trodix.demo.adapter.in.mapper;

import com.trodix.demo.adapter.in.dto.*;
import com.trodix.demo.application.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * Mapper MapStruct pour convertir entre les DTOs de la couche adapter et les modèles de la couche application
 * Architecture hexagonale : l'adapter convertit les DTOs REST vers les modèles métier
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper {

    // ===== Permission Commands =====

    PermissionCommand toCommand(AddPermissionRequest request);

    AddPermissionRequest toRequest(PermissionCommand command);

    // ===== Permission Check =====

    PermissionCheckCommand toCommand(PermissionCheckRequest request);

    PermissionCheckResponse toResponse(PermissionCheckResult result);

    PermissionCheckItem toCheckItem(CheckItem item);

    // ===== User =====

    com.trodix.demo.application.model.UserInfo toModel(com.trodix.demo.adapter.in.dto.UserInfo dto);

    com.trodix.demo.adapter.in.dto.UserInfo toDto(com.trodix.demo.application.model.UserInfo model);

    List<com.trodix.demo.application.model.UserInfo> toModelList(List<com.trodix.demo.adapter.in.dto.UserInfo> dtos);

    default UserListResponse toUserListResponse(List<com.trodix.demo.application.model.UserInfo> users) {
        return new UserListResponse(users.stream().map(this::toDto).toList());
    }

    // ===== User Permissions =====

    UserPermissionsResponse toResponse(UserPermissionsInfo info);

    PermissionTuple toDto(PermissionTupleInfo info);

    List<PermissionTuple> toTupleDtoList(List<PermissionTupleInfo> infos);

    // ===== Entity =====

    com.trodix.demo.application.model.EntityInfo toModel(com.trodix.demo.adapter.in.dto.EntityInfo dto);

    com.trodix.demo.adapter.in.dto.EntityInfo toDto(com.trodix.demo.application.model.EntityInfo model);

    List<com.trodix.demo.application.model.EntityInfo> toEntityModelList(List<com.trodix.demo.adapter.in.dto.EntityInfo> dtos);

    default EntityListResponse toEntityListResponse(List<com.trodix.demo.application.model.EntityInfo> entities) {
        return new EntityListResponse(entities.stream().map(this::toDto).toList());
    }

    // ===== Tenant =====

    com.trodix.demo.application.model.TenantInfo toModel(com.trodix.demo.adapter.in.dto.TenantInfo dto);

    com.trodix.demo.adapter.in.dto.TenantInfo toDto(com.trodix.demo.application.model.TenantInfo model);

    List<com.trodix.demo.application.model.TenantInfo> toTenantModelList(List<com.trodix.demo.adapter.in.dto.TenantInfo> dtos);

    default TenantListResponse toTenantListResponse(List<com.trodix.demo.application.model.TenantInfo> tenants) {
        return new TenantListResponse(tenants.stream().map(this::toDto).toList());
    }

    // ===== Enriched Permissions =====

    EnrichedPermissionsResponse toResponse(EnrichedPermissionsInfo info);

    // ===== Resource Permission =====

    ResourcePermissionStatus toDto(ResourcePermissionInfo info);

    List<ResourcePermissionStatus> toResourcePermissionDtoList(List<ResourcePermissionInfo> infos);

    PermissionStatus toDto(PermissionStatusInfo info);

    // ===== Resource Info =====

    com.trodix.demo.application.model.ResourceInfo toModel(com.trodix.demo.adapter.in.dto.ResourceInfo dto);

    com.trodix.demo.adapter.in.dto.ResourceInfo toDto(com.trodix.demo.application.model.ResourceInfo model);

    List<com.trodix.demo.application.model.ResourceInfo> toResourceInfoModelList(List<com.trodix.demo.adapter.in.dto.ResourceInfo> dtos);
}

