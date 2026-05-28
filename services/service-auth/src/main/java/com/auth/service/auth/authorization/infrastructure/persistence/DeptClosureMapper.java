package com.auth.service.auth.authorization.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 部门闭包表只读 Mapper（dept_closure）
 *
 * @author Bunny
 */
@Mapper
public interface DeptClosureMapper {

	/**
	 * 将锚点部门展开为自身及全部后代的部门 ID（去重、升序）
	 * @param ancestorDeptIds 锚点部门 ID 集合；空集合时返回空列表
	 * @return 后代部门 ID 列表（含闭包 depth=0 的自身节点）
	 */
	List<Long> selectDescendantDeptIdsByAncestorIds(@Param("ancestorDeptIds") Collection<Long> ancestorDeptIds);

}
