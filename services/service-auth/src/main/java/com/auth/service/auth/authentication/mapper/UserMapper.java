package com.auth.service.auth.authentication.mapper;

import com.auth.module.platform.persistence.model.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper（认证专用）
 *
 * @author Bunny
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

	/**
	 * 根据用户名查询用户
	 * @param username 用户名
	 * @return 用户信息
	 */
	UserEntity selectUserByUsername(@Param("username") String username);

	/**
	 * 根据邮箱查询用户
	 * @param email 邮箱
	 * @return 用户信息
	 */
	UserEntity selectUserByEmail(@Param("email") String email);

}
