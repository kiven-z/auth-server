package com.auth.service.system.common.model;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 系统模块选项 VO。
 * <p>
 * 用于前端远程搜索下拉框、模块选择器。
 * name 为枚举标识（如 SYS_USER），type 为模块分类，label 为中文展示名称。
 * </p>
 *
 * @author Bunny
 */
@Data
@Builder
public class ModuleOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块标识，对应枚举 name（如 SYS_USER）
     */
    @Schema(title = "模块标识", description = "枚举名称，全局唯一，如 SYS_USER")
    private String name;

    /**
     * 模块分类（SYS / USER / GRANT / POLICY / AUDIT / LOG）
     */
    @Schema(title = "模块分类")
    private String type;

    /**
     * 模块展示名称（如 用户、角色）
     */
    @Schema(title = "模块展示名称")
    private String label;

}
