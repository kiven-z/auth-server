package com.auth.service.system.admin.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色影响面刷新结果
 *
 * @author Bunny
 */
@Schema(name = "RoleImpactRefreshResultVO", title = "角色影响面刷新结果")
@Getter
@Setter
@ToString
public class RoleImpactRefreshResultVO {

	@Schema(title = "已提交刷新的用户数")
	private Long refreshedUserCount;

}
