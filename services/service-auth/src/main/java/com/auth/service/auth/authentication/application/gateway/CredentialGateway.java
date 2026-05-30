package com.auth.service.auth.authentication.application.gateway;

import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;

import java.util.Optional;

/**
 * 凭证基础设施网关：账户查询、验证码读写、登录失败计数与锁定。
 *
 * @author Bunny
 */
public interface CredentialGateway {

	// --- 账户查询 ---

	/**
	 * 登录场景：仅过滤已删除用户，禁用/锁定由策略层校验。
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户，不存在时为空
	 */
	Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value);

	/**
	 * 发码等场景：仅查询未删除且状态正常的用户。
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户，不存在时为空
	 */
	Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value);

	// --- 验证码 ---

	/**
	 * 读取已存储的验证码摘要（与登录侧 MD5 摘要比对）。
	 * @param dimension 邮箱或手机号维度
	 * @param target 邮箱地址或手机号
	 * @return 存储的摘要，不存在时为 null
	 */
	String getStoredDigest(CredentialDimension dimension, String target);

	/**
	 * 校验成功后删除验证码，防止重放。
	 * @param dimension 邮箱或手机号维度
	 * @param target 邮箱地址或手机号
	 */
	void deleteCode(CredentialDimension dimension, String target);

	/**
	 * 存储验证码摘要（发送侧写入，与登录校验 MD5 摘要比对）。
	 * @param dimension 邮箱或手机号维度
	 * @param target 邮箱地址或手机号
	 * @param digestHex16 摘要（如 MD5 hex16）
	 */
	void storeDigest(CredentialDimension dimension, String target, String digestHex16);

	// --- 失败计数与锁定 ---

	/**
	 * 登录前置只读检查：失败次数达阈值时抛出锁定业务异常。
	 * @param userId 用户 ID
	 */
	void assertNotLocked(Long userId);

	/**
	 * 记录一次认证失败并返回剩余可尝试次数。
	 * @param userId 用户 ID
	 * @return 剩余次数
	 */
	int recordFailure(Long userId);

	/**
	 * 认证成功时清除失败计数。
	 * @param userId 用户 ID
	 */
	void recordSuccess(Long userId);

}
