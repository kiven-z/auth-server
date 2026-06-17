package com.auth.service.system.admin.mapper;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysUserMapper extends BaseMapper<UserEntity> {

	/**
	 * 分页查询用户
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段
	 * @return 分页结果
	 */
	IPage<SysUserPageRowPO> selectListByPage(@Param("page") Page<UserEntity> page,
			@Param("query") SysUserPageQuery query, @Param("orderBySql") String orderBySql);

}
