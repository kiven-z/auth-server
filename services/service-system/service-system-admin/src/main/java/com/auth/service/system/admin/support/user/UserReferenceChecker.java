package com.auth.service.system.admin.support.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.support.BusinessKeyAssert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 用户写操作前的存在性与业务键唯一性校验
 *
 * @author Bunny
 */
@Component
public class UserReferenceChecker {

	private static final String LABEL_USERNAME = "用户名";

	private static final String LABEL_EMAIL = "邮箱";

	private static final String LABEL_PHONE = "手机号";

	private static final String LABEL_EMPLOYEE_NO = "工号";

	private final SysUserMapper sysUserMapper;

	private final SysUserService userService;

	/**
	 * @param sysUserMapper 用户 Mapper
	 * @param userService 用户写服务（延迟注入，避免与自身循环依赖）
	 */
	public UserReferenceChecker(SysUserMapper sysUserMapper, @Lazy SysUserService userService) {
		this.sysUserMapper = sysUserMapper;
		this.userService = userService;
	}

	/**
	 * 加载用户，不存在则抛业务异常
	 * @param userId 用户 ID
	 * @return 用户实体
	 */
	public UserEntity getExistingActive(Long userId) {
		UserEntity existing = sysUserMapper.selectById(userId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND, userId);
		}
		return existing;
	}

	/**
	 * 校验用户名、邮箱、手机号、工号唯一性
	 * @param form 表单
	 * @param excludeUserId 更新时排除的用户 ID，新增时为 null
	 */
	public void assertUniqueBusinessKeys(SysUserForm form, Long excludeUserId) {
		BusinessKeyAssert.requireAbsent(userService,
				buildUniqueKeyQuery(UserEntity::getUsername, form.getUsername(), excludeUserId),
				SystemCommonResultCode.PARAM_DUPLICATE, LABEL_USERNAME, form.getUsername());
		BusinessKeyAssert.requireAbsent(userService,
				buildUniqueKeyQuery(UserEntity::getEmail, form.getEmail(), excludeUserId),
				SystemCommonResultCode.PARAM_DUPLICATE, LABEL_EMAIL, form.getEmail());
		BusinessKeyAssert.requireAbsent(userService,
				buildUniqueKeyQuery(UserEntity::getPhone, form.getPhone(), excludeUserId),
				SystemCommonResultCode.PARAM_DUPLICATE, LABEL_PHONE, form.getPhone());
		if (CharSequenceUtil.isNotBlank(form.getEmployeeNo())) {
			BusinessKeyAssert.requireAbsent(userService,
					buildUniqueKeyQuery(UserEntity::getEmployeeNo, form.getEmployeeNo(), excludeUserId),
					SystemCommonResultCode.PARAM_DUPLICATE, LABEL_EMPLOYEE_NO, form.getEmployeeNo());
		}
	}

	private LambdaQueryWrapper<UserEntity> buildUniqueKeyQuery(SFunction<UserEntity, ?> column, String value,
			Long excludeUserId) {
		var query = Wrappers.<UserEntity>lambdaQuery().eq(column, value);
		if (excludeUserId != null) {
			query.ne(UserEntity::getId, excludeUserId);
		}
		return query;
	}

}
