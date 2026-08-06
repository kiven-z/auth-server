package com.auth.service.example.mapper;

import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
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

}
