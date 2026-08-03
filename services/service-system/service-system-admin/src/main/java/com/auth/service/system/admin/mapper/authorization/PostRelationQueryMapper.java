package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.query.authorization.PostUserPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 岗位关联只读查询
 *
 * @author Bunny
 */
@Mapper
public interface PostRelationQueryMapper {

	/**
	 * 统计岗位关联用户数（user_post.status IN 1,2）
	 * @param postId 岗位 ID
	 * @param query 过滤条件
	 * @return 用户数
	 */
	long countUsersByPostId(@Param("postId") Long postId, @Param("query") PostUserPageQuery query);

	/**
	 * 分页查询岗位关联用户
	 * @param page 分页参数
	 * @param postId 岗位 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<SysPostBoundUserPO> selectUsersByPostIdPage(@Param("page") Page<?> page, @Param("postId") Long postId,
			@Param("query") PostUserPageQuery query);

}
