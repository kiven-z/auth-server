package com.auth.service.system.admin.model.vo.role;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色授权对象按主体类型回显（grant_table 角色视角）
 *
 * @author Bunny
 */
@Schema(name = "SysRoleGrantSubjectsVO", title = "角色授权对象按类型回显")
@Getter
@Setter
@ToString
public class SysRoleGrantSubjectsVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "查询的主体类型")
	private GrantTableSubjectType subjectType;

	@Schema(title = "已授权主体列表（空表示该类型无授权）")
	private List<GrantSubjectItemVO> subjects = new ArrayList<>();

}
