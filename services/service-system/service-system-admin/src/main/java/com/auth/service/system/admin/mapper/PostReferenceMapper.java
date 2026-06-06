package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 岗位删除前的引用只读查询
 *
 * @author Bunny
 */
@Mapper
public interface PostReferenceMapper {

	/**
	 * 是否存在有效的用户-岗位关联
	 * @param postId 岗位 ID
	 * @return 引用行数
	 */
	long countUserPostByPostId(@Param("postId") Long postId);

	/**
	 * 是否存在 grant_table 的 POST 主体引用
	 * @param postId 岗位 ID
	 * @return 引用行数
	 */
	long countGrantPostSubjectByPostId(@Param("postId") Long postId);

}
