package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.po.authorizationinvalidation.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.admin.model.po.authorizationinvalidation.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationOutboxQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 授权失效 Outbox 管理端只读 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationOutboxAdminMapper {

	/**
	 * 分页查询 Outbox（运维只读）
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<AuthorizationInvalidationOutboxPageRowPO> selectListByPage(
			@Param("page") Page<AuthorizationInvalidationOutboxPageRowPO> page,
			@Param("query") AuthorizationInvalidationOutboxQuery query);

	/**
	 * 按主键查询 Outbox 详情
	 * @param id 主键
	 * @return 详情行，不存在为 null
	 */
	AuthorizationInvalidationOutboxDetailRowPO selectDetailById(@Param("id") Long id);

}
