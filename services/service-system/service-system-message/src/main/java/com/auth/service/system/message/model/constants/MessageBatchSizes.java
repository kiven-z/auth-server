package com.auth.service.system.message.model.constants;

import lombok.experimental.UtilityClass;

/**
 * message 模块批量处理批次大小。
 *
 * @author Bunny
 */
@UtilityClass
public class MessageBatchSizes {

	/**
	 * 渠道投递记录写入/更新分片
	 */
	public static final int DELIVERY_WRITE = 500;

	/**
	 * 站内信收件箱批量插入分片
	 */
	public static final int RECIPIENT_INSERT = 500;

	/**
	 * 站内信群发接收人游标扫描每批大小
	 */
	public static final int RECIPIENT_SCAN = 1000;

}
