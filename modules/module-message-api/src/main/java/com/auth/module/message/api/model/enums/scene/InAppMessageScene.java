package com.auth.module.message.api.model.enums.scene;

import com.auth.module.message.api.channel.MessageChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 站内信渠道消息场景
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum InAppMessageScene {

	/**
	 * 文件导出成功
	 */
	FILE_EXPORT_SUCCESS("file-export-success", 5),

	/**
	 * 文件导出失败
	 */
	FILE_EXPORT_FAILED("file-export-failed", 5);

	/**
	 * 模板/场景编码
	 */
	private final String templateCode;

	/**
	 * 同 scene_code + channel 多条时的权重（越大越高）
	 */
	private final int priority;

	/**
	 * 发送渠道
	 * @return 站内信渠道
	 */
	public MessageChannel getChannel() {
		return MessageChannel.IN_APP;
	}

}
