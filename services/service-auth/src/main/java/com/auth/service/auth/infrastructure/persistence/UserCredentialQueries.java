package com.auth.service.auth.infrastructure.persistence;

import com.auth.module.platform.persistence.model.UserEntity;
import lombok.experimental.UtilityClass;

/**
 * {@link UserMapper} 凭证查询语义化入口，避免调用方散落 {@code activeOnly} 布尔含义。
 *
 * @author Bunny
 */
@UtilityClass
public class UserCredentialQueries {

	/**
	 * 登录场景：仅过滤已删除用户，禁用/锁定由策略层校验。
	 * @param userMapper 用户 Mapper
	 * @param field 凭证维度
	 * @param value 凭证值
	 * @return 用户实体，不存在时为 null
	 */
	public static UserEntity forLogin(UserMapper userMapper, UserCredentialField field, String value) {
		return userMapper.selectByCredential(field, value, false);
	}

	/**
	 * 发码等场景：仅查询未删除且状态正常的用户。
	 * @param userMapper 用户 Mapper
	 * @param field 凭证维度
	 * @param value 凭证值
	 * @return 用户实体，不存在时为 null
	 */
	public static UserEntity activeOnly(UserMapper userMapper, UserCredentialField field, String value) {
		return userMapper.selectByCredential(field, value, true);
	}

}
