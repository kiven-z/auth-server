package com.auth.service.system.schedule.task.builtin.model;

/**
 * 内置任务 {@code job_params} 载荷版本约定（与 {@code MESSAGE_DISPATCH} 的 payload 信封对齐）
 *
 * @author Bunny
 */
public interface VersionedJobParams {

	/**
	 * 当前支持的载荷版本
	 */
	int VERSION_1 = 1;

	/**
	 * 载荷协议版本
	 * @return 版本号
	 */
	Integer getVersion();

	/**
	 * 设置载荷协议版本
	 * @param version 版本号
	 */
	void setVersion(Integer version);

}
