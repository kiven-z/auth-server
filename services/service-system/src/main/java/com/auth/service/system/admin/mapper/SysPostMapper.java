package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 系统岗位 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPostEntity> {

	/**
	 * 分页查询岗位（关联部门名称；创建人/更新人用户名由
	 * {@link com.auth.service.system.common.service.AuditUserDisplayService} 补全）
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段（仅列名与 ASC/DESC）
	 * @return 分页结果
	 */
	IPage<SysPostPageRowPO> selectListByPage(@Param("page") Page<SysPostEntity> page,
			@Param("query") SysPostQuery query, @Param("orderBySql") String orderBySql);

	/**
	 * 统计可用于挂载岗位的部门（存在、启用、未删除）
	 * @param deptId 部门主键
	 * @return 命中条数（0 或 1）
	 */
	int countAssignableDept(@Param("deptId") Long deptId);

}
