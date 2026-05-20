package com.auth.service.auth.authentication.infrastructure.persistence;

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
	 * 根据用户名查询可登录用户（仅未删除；含禁用/锁定，由登录策略做状态校验）
	 * @param username 用户名
	 * @return 用户信息
	 */
	UserEntity selectUserForLoginByUsername(@Param("username") String username);

	/**
	 * 根据邮箱查询可登录用户（仅未删除；含禁用/锁定，由登录策略做状态校验）
	 * @param email 邮箱
	 * @return 用户信息
	 */
	UserEntity selectUserForLoginByEmail(@Param("email") String email);

	/**
	 * 根据手机号查询可登录用户（仅未删除；含禁用/锁定，由登录策略做状态校验）
	 * @param phone 手机号
	 * @return 用户信息
	 */
	UserEntity selectUserForLoginByPhone(@Param("phone") String phone);

	/**
	 * 根据用户名查询正常状态用户（发码等场景）
	 * @param username 用户名
	 * @return 用户信息
	 */
	UserEntity selectUserByUsername(@Param("username") String username);

	/**
	 * 根据邮箱查询正常状态用户（发码等场景）
	 * @param email 邮箱
	 * @return 用户信息
	 */
	UserEntity selectUserByEmail(@Param("email") String email);

	/**
	 * 根据手机号查询正常状态用户（发码等场景）
	 * @param phone 手机号
	 * @return 用户信息
	 */
	UserEntity selectUserByPhone(@Param("phone") String phone);

}
