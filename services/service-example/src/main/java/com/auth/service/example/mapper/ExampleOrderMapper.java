package com.auth.service.example.mapper;

import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据权限演示单 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface ExampleOrderMapper extends BaseMapper<ExampleOrderEntity> {

	/**
	 * 按登录画像过滤的演示单列表
	 * @return 可见行
	 */
	@DataScope(alias = "d")
	@Select("""
			SELECT d.id, d.title, d.dept_id, d.created_by, d.updated_by,
			       d.created_at, d.updated_at, d.version, d.remark
			FROM example_order d
			ORDER BY d.id
			""")
	List<ExampleOrderEntity> selectScopedList();

	/**
	 * 按登录画像分页查询演示单
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	@DataScope(alias = "d")
	IPage<ExampleOrderEntity> selectScopedPage(Page<ExampleOrderEntity> page, @Param("query") ExampleOrderQuery query);

}
