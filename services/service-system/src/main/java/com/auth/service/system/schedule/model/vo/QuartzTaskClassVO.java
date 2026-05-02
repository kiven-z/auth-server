package com.auth.service.system.schedule.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前端「执行配置」下拉：白名单任务类信息
 *
 * @author Bunny
 */
@Schema(name = "QuartzTaskClassVO", title = "可调度任务类")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzTaskClassVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "类全限定名")
	private String className;

	@Schema(title = "展示名称")
	private String name;

	@Schema(title = "说明")
	private String description;

}
