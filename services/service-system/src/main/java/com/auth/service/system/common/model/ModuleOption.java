package com.auth.service.system.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统模块选项 VO。
 * <p>
 * 用于前端远程搜索下拉框、模块选择器，value 为数据库表名，label 为中文描述。
 * </p>
 *
 * @author Bunny
 */
@Data
@Builder
public class ModuleOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(title = "模块描述")
    private String name;

    @Schema(title = "模块分类")
    private String type;

    @Schema(title = "模块展示名称")
    private String label;

}
