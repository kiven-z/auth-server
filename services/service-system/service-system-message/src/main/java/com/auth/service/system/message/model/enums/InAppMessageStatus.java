package com.auth.service.system.message.model.enums;

/**
 * 消息发送任务状态
 *
 * @author Bunny
 */
public enum InAppMessageStatus {

	/**
	 * 待发送
	 */
	PENDING,

	/**
	 * 发送中
	 */
	SENDING,

	/**
	 * 全部成功
	 */
	SUCCESS,

	/**
	 * 部分成功
	 */
	PARTIAL,

	/**
	 * 失败
	 */
	FAILED,

	/**
	 * 展开后无有效接收人
	 */
	NO_RECIPIENTS,

	/**
	 * 已撤回
	 */
	RECALLED

}
