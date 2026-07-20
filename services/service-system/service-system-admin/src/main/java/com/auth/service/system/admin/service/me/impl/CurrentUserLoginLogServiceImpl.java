package com.auth.service.system.admin.service.me.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.service.admin.LogLoginQueryService;
import com.auth.service.system.admin.service.me.CurrentUserLoginLogService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人中心登录日志查询实现：强制当前用户与 180 天窗口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CurrentUserLoginLogServiceImpl implements CurrentUserLoginLogService {

	/**
	 * 个人中心登录日志最大可查天数
	 */
	private static final int ME_LOGIN_LOG_MAX_DAYS = 180;

	private final LogLoginQueryService logLoginQueryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<MeLoginLogPageVO> getPage(Page<LoginLogEntity> pageParams, MeLoginLogPageQuery query) {
		Long userId = SecurityUserUtils.getUserId();
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.JWT_INVALID);
		}
		LogLoginLogQuery internalQuery = new LogLoginLogQuery();
		internalQuery.setPageIndex(query.getPageIndex());
		internalQuery.setPageSize(query.getPageSize());
		internalQuery.setLoginType(query.getLoginType());
		internalQuery.setLoginResult(query.getLoginResult());
		internalQuery.setUserId(userId);

		LocalDateTime now = LocalDateTime.now();
		internalQuery.setLoginTimeStart(now.minusDays(ME_LOGIN_LOG_MAX_DAYS));
		internalQuery.setLoginTimeEnd(now);
		PageResponse<LogLoginLogPageVO> adminPage = logLoginQueryService.getPage(pageParams, internalQuery);

		// 批量转换登录日志
		List<MeLoginLogPageVO> meList = adminPage.getList().stream().map(adminVo -> {
			MeLoginLogPageVO vo = new MeLoginLogPageVO();
			vo.setLoginTime(adminVo.getLoginTime());
			vo.setLoginRegion(adminVo.getLoginRegion());
			vo.setLoginResult(adminVo.getLoginResult());
			vo.setLoginType(adminVo.getLoginType());
			return vo;
		}).toList();
		return PageResponse.of(adminPage.getPageNo(), adminPage.getPageSize(), adminPage.getTotal(), meList);
	}

}
