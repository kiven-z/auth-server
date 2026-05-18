package com.auth.service.system.schedule.model.payload;

import com.auth.module.message.command.TemplateMessageCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息派发任务载荷（payload_json 标准结构）
 *
 * @author Bunny
 */
@Schema(name = "MessageDispatchPayload", title = "消息派发任务载荷")
@Getter
@Setter
public class MessageDispatchPayload implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 当前支持的载荷版本
	 */
	public static final int VERSION_1 = 1;

	@Schema(title = "载荷协议版本")
	@NotNull
	private Integer version = VERSION_1;

	@Schema(title = "消息命令")
	@NotNull
	private TemplateMessageCommand command;

}
