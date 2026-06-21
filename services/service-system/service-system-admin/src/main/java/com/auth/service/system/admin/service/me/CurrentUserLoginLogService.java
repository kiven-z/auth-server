package com.auth.service.system.admin.service.me;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 个人中心登录日志分页查询
 *
 * @author Bunny
 */
public interface CurrentUserLoginLogService {

	/**
	 * 分页查询当前用户最近 180 天内的登录日志
	 * @param pageParams 分页参数
	 * @param query 客户端查询条件（loginType、loginResult）
	 * @return 精简分页数据
	 */
	PageResponse<MeLoginLogPageVO> getPage(Page<LoginLogEntity> pageParams, MeLoginLogPageQuery query);

}
