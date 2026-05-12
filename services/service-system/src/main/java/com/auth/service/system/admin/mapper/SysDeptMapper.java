package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}
