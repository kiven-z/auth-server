package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.po.InAppInboxMajorUnreadRowPO;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户侧站内信收件箱 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface InAppInboxMapper {

	/**
	 * 分页查询当前用户收件箱
	 * @param page 分页参数
	 * @param userId 当前用户 ID
	 * @param query 查询条件
	 * @return 分页行
	 */
	IPage<InAppInboxPageRowPO> selectInboxPage(Page<InAppInboxPageRowPO> page, @Param("userId") Long userId,
			@Param("query") InAppInboxQuery query);

	/**
	 * 启用大类全量 + 各未读数
	 * @param userId 当前用户 ID
	 * @return 大类未读行
	 */
	List<InAppInboxMajorUnreadRowPO> selectUnreadCountByMajor(@Param("userId") Long userId);

}
