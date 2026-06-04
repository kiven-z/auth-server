package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统岗位 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPostEntity> {

	/**
	 * 分页查询岗位
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段
	 * @return 分页结果
	 */
	IPage<SysPostPageRowPO> selectListByPage(@Param("page") Page<SysPostEntity> page,
			@Param("query") SysPostQuery query, @Param("orderBySql") String orderBySql);

	/**
	 * 统计可用于挂载岗位的部门
	 * @param deptId 部门主键
	 * @return 命中条数
	 */
	@Select("SELECT COUNT(1) FROM sys_dept WHERE id = #{deptId} AND status = 1 AND is_deleted = 0")
	int countAssignableDept(@Param("deptId") Long deptId);

	/**
	 * 统计岗位下仍有效的用户-岗位关联（与授权影响面反查一致）
	 * @param postId 岗位主键
	 * @return 关联行数
	 */
	@Select("SELECT COUNT(1) FROM user_post WHERE post_id = #{postId} AND status IN (1, 2)")
	long countActiveUserPostByPostId(@Param("postId") Long postId);

}
