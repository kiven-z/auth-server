package com.auth.service.system.admin.mapper.post;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 岗位删除前的引用只读查询
 *
 * @author Bunny
 */
@Mapper
public interface PostReferenceCheckerMapper {

	/**
	 * 是否存在有效的用户-岗位关联
	 * @param postId 岗位 ID
	 * @return 引用行数
	 */
	@Select("SELECT COUNT(1) FROM user_post up WHERE up.post_id = #{postId} AND up.status IN (1, 2)")
	long countUserPostByPostId(@Param("postId") Long postId);

	/**
	 * 是否存在 grant_table 的 POST 主体引用
	 * @param postId 岗位 ID
	 * @return 引用行数
	 */
	@Select("SELECT COUNT(1) FROM grant_table gt WHERE gt.subject_type = 'POST' AND gt.subject_id = #{postId}")
	long countGrantPostSubjectByPostId(@Param("postId") Long postId);

}
