package com.trodix.demo.application.service;

import com.trodix.demo.adapter.in.dto.PermissionStatus;
import com.trodix.demo.adapter.in.dto.ResourceInfo;
import com.trodix.demo.adapter.in.dto.ResourcePermissionStatus;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourcePermissionCheckerTest {

    @Mock
    private OpenFgaClient fgaClient;

    @InjectMocks
    private ResourcePermissionChecker resourcePermissionChecker;

    private Set<String> directPermissions;

    @BeforeEach
    void setUp() {
        directPermissions = new HashSet<>();
        directPermissions.add("entity:e1#read");
        directPermissions.add("entity:e1#write");
        directPermissions.add("product:p1#read");
        directPermissions.add("tenant:t1#member");
    }

    @Test
    void checkResourcePermissions_shouldReturnDirectPermission() throws Exception {
        // Given
        String username = "testuser";
        String resourceType = "entity";
        String resourceId = "e1";
        List<String> relations = List.of("read", "write", "delete");

        ClientCheckResponse checkResponseRead = mock(ClientCheckResponse.class);
        when(checkResponseRead.getAllowed()).thenReturn(true);

        ClientCheckResponse checkResponseWrite = mock(ClientCheckResponse.class);
        when(checkResponseWrite.getAllowed()).thenReturn(true);

        ClientCheckResponse checkResponseDelete = mock(ClientCheckResponse.class);
        when(checkResponseDelete.getAllowed()).thenReturn(false);

        when(fgaClient.check(any(ClientCheckRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(checkResponseRead))
            .thenReturn(CompletableFuture.completedFuture(checkResponseWrite))
            .thenReturn(CompletableFuture.completedFuture(checkResponseDelete));

        // When
        Map<String, PermissionStatus> result = resourcePermissionChecker.checkResourcePermissions(
            username, resourceType, resourceId, relations, directPermissions
        );

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get("read")).isEqualTo(new PermissionStatus(true, true)); // Direct
        assertThat(result.get("write")).isEqualTo(new PermissionStatus(true, true)); // Direct
        assertThat(result.get("delete")).isEqualTo(new PermissionStatus(false, false)); // No permission
    }

    @Test
    void checkResourcePermissions_shouldReturnIndirectPermission() throws Exception {
        // Given
        String username = "testuser";
        String resourceType = "entity";
        String resourceId = "e2"; // Not in direct permissions
        List<String> relations = List.of("read");

        ClientCheckResponse checkResponse = mock(ClientCheckResponse.class);
        when(checkResponse.getAllowed()).thenReturn(true);

        when(fgaClient.check(any(ClientCheckRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(checkResponse));

        // When
        Map<String, PermissionStatus> result = resourcePermissionChecker.checkResourcePermissions(
            username, resourceType, resourceId, relations, directPermissions
        );

        // Then
        assertThat(result.get("read")).isEqualTo(new PermissionStatus(true, false)); // Indirect
    }

    @Test
    void checkResourcePermissions_shouldHandleEmptyRelations() throws Exception {
        // Given
        String username = "testuser";
        String resourceType = "entity";
        String resourceId = "e1";
        List<String> relations = List.of();

        // When
        Map<String, PermissionStatus> result = resourcePermissionChecker.checkResourcePermissions(
            username, resourceType, resourceId, relations, directPermissions
        );

        // Then
        assertThat(result).isEmpty();
        verify(fgaClient, never()).check(any());
    }

    @Test
    void checkResourcePermissions_shouldThrowExceptionOnFgaError() throws Exception {
        // Given
        String username = "testuser";
        String resourceType = "entity";
        String resourceId = "e1";
        List<String> relations = List.of("read");

        when(fgaClient.check(any(ClientCheckRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("FGA error")));

        // When/Then
        assertThatThrownBy(() ->
            resourcePermissionChecker.checkResourcePermissions(
                username, resourceType, resourceId, relations, directPermissions
            )
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Error checking permission");
    }

    @Test
    void checkMultipleResources_shouldReturnPermissionsForAllResources() throws Exception {
        // Given
        String username = "testuser";
        String resourceType = "entity";
        List<ResourceInfo> resources = List.of(
            new ResourceInfo("e1", "Entity 1"),
            new ResourceInfo("e2", "Entity 2")
        );
        List<String> relations = List.of("read");

        ClientCheckResponse checkResponse = mock(ClientCheckResponse.class);
        when(checkResponse.getAllowed()).thenReturn(true);

        when(fgaClient.check(any(ClientCheckRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(checkResponse));

        // When
        List<ResourcePermissionStatus> result = resourcePermissionChecker.checkMultipleResources(
            username, resourceType, resources, relations, directPermissions
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).resourceId()).isEqualTo("e1");
        assertThat(result.get(0).resourceName()).isEqualTo("Entity 1");
        assertThat(result.get(0).permissions().get("read")).isNotNull();
        assertThat(result.get(1).resourceId()).isEqualTo("e2");
        assertThat(result.get(1).resourceName()).isEqualTo("Entity 2");
    }

    @Test
    void checkMultipleResources_shouldHandleEmptyResourceList() throws Exception {
        // Given
        String username = "testuser";
        String resourceType = "entity";
        List<ResourceInfo> resources = List.of();
        List<String> relations = List.of("read");

        // When
        List<ResourcePermissionStatus> result = resourcePermissionChecker.checkMultipleResources(
            username, resourceType, resources, relations, directPermissions
        );

        // Then
        assertThat(result).isEmpty();
        verify(fgaClient, never()).check(any());
    }

    @Test
    void getDirectPermissions_shouldReturnAllDirectPermissionsForUser() throws Exception {
        // Given
        String username = "testuser";

        var tuple1Key = mock(dev.openfga.sdk.api.model.TupleKey.class);
        when(tuple1Key.getUser()).thenReturn("user:testuser");
        when(tuple1Key.getObject()).thenReturn("entity:e1");
        when(tuple1Key.getRelation()).thenReturn("read");

        var tuple2Key = mock(dev.openfga.sdk.api.model.TupleKey.class);
        when(tuple2Key.getUser()).thenReturn("user:testuser");
        when(tuple2Key.getObject()).thenReturn("entity:e1");
        when(tuple2Key.getRelation()).thenReturn("write");

        var tuple3Key = mock(dev.openfga.sdk.api.model.TupleKey.class);
        when(tuple3Key.getUser()).thenReturn("user:otheruser"); // Different user
        lenient().when(tuple3Key.getObject()).thenReturn("entity:e2");
        lenient().when(tuple3Key.getRelation()).thenReturn("read");

        var mockTuple1 = mock(dev.openfga.sdk.api.model.Tuple.class);
        when(mockTuple1.getKey()).thenReturn(tuple1Key);

        var mockTuple2 = mock(dev.openfga.sdk.api.model.Tuple.class);
        when(mockTuple2.getKey()).thenReturn(tuple2Key);

        var mockTuple3 = mock(dev.openfga.sdk.api.model.Tuple.class);
        when(mockTuple3.getKey()).thenReturn(tuple3Key);

        ClientReadResponse readResponse = mock(ClientReadResponse.class);
        when(readResponse.getTuples()).thenReturn(List.of(mockTuple1, mockTuple2, mockTuple3));

        when(fgaClient.read(any(ClientReadRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(readResponse));

        // When
        Set<String> result = resourcePermissionChecker.getDirectPermissions(username);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains("entity:e1#read");
        assertThat(result).contains("entity:e1#write");
        assertThat(result).doesNotContain("entity:e2#read"); // Different user
    }

    @Test
    void getDirectPermissions_shouldReturnEmptySetWhenNoPermissions() throws Exception {
        // Given
        String username = "testuser";

        ClientReadResponse readResponse = mock(ClientReadResponse.class);
        when(readResponse.getTuples()).thenReturn(List.of());

        when(fgaClient.read(any(ClientReadRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(readResponse));

        // When
        Set<String> result = resourcePermissionChecker.getDirectPermissions(username);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getDirectPermissions_shouldThrowExceptionOnFgaError() throws FgaInvalidParameterException {
        // Given
        String username = "testuser";

        when(fgaClient.read(any(ClientReadRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("FGA read error")));

        // When/Then
        assertThatThrownBy(() ->
            resourcePermissionChecker.getDirectPermissions(username)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Error reading direct permissions");
    }

    @Test
    void checkResourcePermissions_shouldWorkForDifferentResourceTypes() throws Exception {
        // Test for tenant
        ClientCheckResponse checkResponse = mock(ClientCheckResponse.class);
        when(checkResponse.getAllowed()).thenReturn(true);
        when(fgaClient.check(any(ClientCheckRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(checkResponse));

        Map<String, PermissionStatus> tenantResult = resourcePermissionChecker.checkResourcePermissions(
            "testuser", "tenant", "t1", List.of("member"), directPermissions
        );
        assertThat(tenantResult.get("member")).isEqualTo(new PermissionStatus(true, true));

        // Test for product
        Map<String, PermissionStatus> productResult = resourcePermissionChecker.checkResourcePermissions(
            "testuser", "product", "p1", List.of("read"), directPermissions
        );
        assertThat(productResult.get("read")).isEqualTo(new PermissionStatus(true, true));

        // Test for entity
        Map<String, PermissionStatus> entityResult = resourcePermissionChecker.checkResourcePermissions(
            "testuser", "entity", "e1", List.of("read"), directPermissions
        );
        assertThat(entityResult.get("read")).isEqualTo(new PermissionStatus(true, true));
    }
}
