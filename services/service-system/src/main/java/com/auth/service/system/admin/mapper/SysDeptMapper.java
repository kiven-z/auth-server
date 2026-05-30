package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统部门 Mapper。
 *
 * @author Bunny
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDeptEntity> {

	/**
	 * 条件查询全部部门（扁平，供前端建树）。
	 * @param query 查询条件
	 * @return 部门列表
	 */
	List<SysDeptEntity> selectListByQuery(@Param("query") SysDeptQuery query);

	/**
	 * 分页查询部门（表格视图）。
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页部门列表
	 */
	IPage<SysDeptEntity> selectPageByQuery(IPage<SysDeptEntity> page, @Param("query") SysDeptQuery query);

	/**
	 * 未逻辑删除的部门数量。
	 * @param deptId 部门 ID
	 * @return 匹配行数
	 */
	@Select("SELECT COUNT(1) FROM sys_dept WHERE id = #{deptId} AND is_deleted = 0")
	int countActiveById(@Param("deptId") Long deptId);

	/**
	 * 未删除的直接子部门数量。
	 * @param parentId 父部门 ID
	 * @return 子部门行数
	 */
	@Select("SELECT COUNT(1) FROM sys_dept WHERE parent_id = #{parentId} AND is_deleted = 0")
	int countActiveDirectChildren(@Param("parentId") Long parentId);

}
