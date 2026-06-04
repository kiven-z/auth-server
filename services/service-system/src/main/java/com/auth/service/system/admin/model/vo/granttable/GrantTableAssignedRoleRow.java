package com.auth.service.system.admin.model.vo.granttable;

import com.auth.service.system.admin.model.vo.role.RoleCodeNameRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * grant_table 主体已分配角色列表行
 *
 * @author Bunny
 */
@Schema(name = "GrantTableAssignedRoleRow", title = "grant_table 已分配角色行")
@Getter
@Setter
public class GrantTableAssignedRoleRow extends RoleCodeNameRow {

}
