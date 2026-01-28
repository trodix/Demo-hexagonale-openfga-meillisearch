export interface CheckItem {
  object: string;
  relation: string;
}

export interface PermissionCheckRequest {
  checks: CheckItem[];
}

export interface PermissionCheckResponse {
  results: { [key: string]: boolean };
}

export interface UserInfo {
  username: string;
  displayName: string;
}

export interface UserListResponse {
  users: UserInfo[];
}

export interface PermissionTuple {
  objectType: string;
  objectId: string;
  relation: string;
}

export interface UserPermissionsResponse {
  username: string;
  tenantId: string;
  permissions: PermissionTuple[];
}

export interface AddPermissionRequest {
  username: string;
  objectType: string;
  objectId: string;
  relation: string;
}

export interface EntityInfo {
  id: string;
  name: string;
  availableRelations: string[];
}

export interface EntityListResponse {
  entities: EntityInfo[];
}

export interface TenantInfo {
  id: string;
  name: string;
}

export interface TenantListResponse {
  tenants: TenantInfo[];
}

export interface EntityPermissionRow {
  entity: EntityInfo;
  hasRead: boolean;
  hasWrite: boolean;
  hasDelete: boolean;
  readIsIndirect: boolean;
  writeIsIndirect: boolean;
  deleteIsIndirect: boolean;
}

export interface ProductPermissionRow {
  productId: string;
  hasRead: boolean;
  hasWrite: boolean;
  hasDelete: boolean;
  readIsIndirect: boolean;
  writeIsIndirect: boolean;
  deleteIsIndirect: boolean;
}

export interface TenantMembershipRow {
  tenant: TenantInfo;
  isMember: boolean;
  isAdmin: boolean;
  isCurrentTenantForCurrentUser: boolean;
}

export interface TenantPermissionStatusDto {
  tenantId: string;
  tenantName: string;
  isMember: boolean;
  isAdmin: boolean;
}

export interface PermissionStatus {
  hasPermission: boolean;
  isDirect: boolean;
}

export interface EntityPermissionStatusDto {
  entityId: string;
  entityName: string;
  read: PermissionStatus;
  write: PermissionStatus;
  delete: PermissionStatus;
}

export interface ProductPermissionStatusDto {
  productId: string;
  read: PermissionStatus;
  write: PermissionStatus;
  delete: PermissionStatus;
}

export interface EnrichedPermissionsResponse {
  username: string;
  tenantId: string;
  tenants: TenantPermissionStatusDto[];
  entities: EntityPermissionStatusDto[];
  products: ProductPermissionStatusDto[];
}
