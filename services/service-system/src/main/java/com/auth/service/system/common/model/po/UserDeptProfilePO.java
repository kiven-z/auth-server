package com.auth.service.system.common.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户档案-部门关联查询行（Mapper 投影，非 API 出参）
 *
 * @author Bunny
 */
@Data
public class UserDeptProfilePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "状态")
	private Integer status;

	@Schema(title = "岗位")
	private String position;

	@Schema(title = "加入时间")
	private LocalDateTime joinTime;

}
