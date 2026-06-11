package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.po.post.SysPostSearchItemPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
	int countAssignableDept(@Param("deptId") Long deptId);

	/**
	 * 岗位关键词搜索（编码/名称前缀匹配，限制条数；无关键字时返回默认列表）
	 * @param keyword 搜索关键字（前缀匹配，可选）
	 * @param status 启用状态（可选）
	 * @param limit 返回条数上限
	 * @return 搜索行列表
	 */
	List<SysPostSearchItemPO> search(@Param("keyword") String keyword, @Param("status") Boolean status,
			@Param("limit") int limit);

}
