package com.auth.service.system.log.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogJobPageResponse", title = "任务日志分页行")
@Data
@EqualsAndHashCode(callSuper = true)
public class LogJobPageResponse extends BaseResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonStringFormat
    @Schema(title = "主键")
    private Long id;

    @Schema(title = "任务名称")
    private String jobName;

    @Schema(title = "任务分组")
    private String jobGroup;

    @Schema(title = "调用目标")
    private String invokeTarget;

    @Schema(title = "执行报告")
    private String jobMessage;

    @Schema(title = "状态(0成功 1失败)")
    private Integer status;

    @Schema(title = "耗时毫秒")
    private Long elapsedTime;

}
