package com.auth.service.system.admin.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色影响面摘要
 *
 * @author Bunny
 */
@Schema(name = "RoleImpactSummaryVO", title = "角色影响面摘要")
@Getter
@Setter
@ToString
public class RoleImpactSummaryVO {

	@Schema(title = "授权对象 USER 数")
	private Long grantUserCount;

	@Schema(title = "授权对象 DEPT 数")
	private Long grantDeptCount;

	@Schema(title = "授权对象 POST 数")
	private Long grantPostCount;

	@Schema(title = "影响用户总数（展开后去重）")
	private Long impactedUserCount;

}
