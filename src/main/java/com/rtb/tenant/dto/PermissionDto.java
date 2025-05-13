package com.rtb.tenant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rtb.core.entity.user.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionDto {
    private Long id;
    private String permissionName;
    private String permissionDescription;

    public PermissionDto(Permission permission) {
        this.id = permission.getId();
        this.permissionName = permission.getPermissionName();
        this.permissionDescription = permission.getPermissionDescription();
    }
}
