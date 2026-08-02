package com.auth.service.system.message.model.constants;

import lombok.experimental.UtilityClass;

/**
 * message 模块批量处理批次大小
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

	/**
	 * 收件箱全部已读：未读 ID 分片后再走标已读
	 */
	public static final int INBOX_MARK_READ = 500;

	/**
	 * 收件箱全部删除：可见 ID 分片后再走软删除
	 */
	public static final int INBOX_DELETE = 500;

}
