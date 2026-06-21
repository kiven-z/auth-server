package com.auth.service.system.admin.service.me.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.me.MePersonalCenterConverter;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.query.me.MeLoginLogQueryAssembler;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.service.admin.LogLoginQueryService;
import com.auth.service.system.admin.service.me.CurrentUserLoginLogService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.JWT_INVALID;

/**
 * 个人中心登录日志查询实现：强制当前用户与 180 天窗口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CurrentUserLoginLogServiceImpl implements CurrentUserLoginLogService {

	private final LogLoginQueryService logLoginQueryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<MeLoginLogPageVO> getPage(Page<LoginLogEntity> pageParams, MeLoginLogPageQuery query) {
		Long userId = SecurityUserUtils.getUserId();
		if (userId == null) {
			throw new SystemBusinessException(JWT_INVALID);
		}

		LogLoginLogQuery internalQuery = MeLoginLogQueryAssembler.toLogLoginLogQuery(userId, query);
		PageResponse<LogLoginLogPageVO> adminPage = logLoginQueryService.getPage(pageParams, internalQuery);

		List<MeLoginLogPageVO> meList = MePersonalCenterConverter.INSTANCE.toLoginLogVoList(adminPage.getList());
		return PageResponse.of(adminPage.getPageNo(), adminPage.getPageSize(), adminPage.getTotal(), meList);
	}

}
