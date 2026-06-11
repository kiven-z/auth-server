package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.po.userpost.UserPostPageRowPO;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户岗位关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPostEntity> {

	/**
	 * 分页查询用户岗位关联
	 * @param page 分页参数
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<UserPostPageRowPO> selectListByPage(@Param("page") Page<UserPostEntity> page, @Param("userId") Long userId,
			@Param("query") UserPostPageQuery query);

}
