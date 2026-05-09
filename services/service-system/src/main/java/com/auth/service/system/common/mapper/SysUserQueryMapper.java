package com.auth.service.system.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.common.model.SysUserSearchItemResponse;
import com.auth.service.system.common.model.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.common.model.vo.SysUserPostProfileItemResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 用户只读查询（档案部门/岗位、关键词搜索）
 *
 * @author Bunny
 */
@Mapper
public interface SysUserQueryMapper extends BaseMapper<UserEntity> {

	/**
	 * 按用户ID查询部门关联列表
	 * @param userId 用户ID
	 * @return 部门项
	 */
	List<SysUserDeptProfileItemResponse> selectDeptProfileByUserId(@Param("userId") Long userId);

	/**
	 * 按用户ID查询岗位关联列表
	 * @param userId 用户ID
	 * @return 岗位项
	 */
	List<SysUserPostProfileItemResponse> selectPostProfileByUserId(@Param("userId") Long userId);

	/**
	 * 关键词模糊搜索用户（未删除、状态正常），匹配用户名/昵称/邮箱/手机/工号。
	 * @param keyword 关键字（非空，由调用方保证）
	 * @param limit 最大返回条数
	 * @return 用户列表，不含密码等敏感列
	 */
	List<SysUserSearchItemResponse> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

}
