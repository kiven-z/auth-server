package com.auth.service.system.admin.support.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 用户写操作前的存在性、业务键唯一性与受保护账号校验。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserReferenceChecker {

	private final SysUserMapper sysUserMapper;

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
		LambdaQueryWrapper<UserEntity> usernameWrapper = buildUniqueKeyQuery(UserEntity::getUsername,
				form.getUsername(), excludeUserId);
		BusinessKeyAssert.requireAbsent(sysUserMapper, usernameWrapper,
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "用户名", form.getUsername()));

		LambdaQueryWrapper<UserEntity> emailWrapper = buildUniqueKeyQuery(UserEntity::getEmail, form.getEmail(),
				excludeUserId);
		BusinessKeyAssert.requireAbsent(sysUserMapper, emailWrapper,
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "邮箱", form.getEmail()));

		LambdaQueryWrapper<UserEntity> phoneWrapper = buildUniqueKeyQuery(UserEntity::getPhone, form.getPhone(),
				excludeUserId);
		BusinessKeyAssert.requireAbsent(sysUserMapper, phoneWrapper,
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "手机号", form.getPhone()));

		if (CharSequenceUtil.isNotBlank(form.getEmployeeNo())) {
			LambdaQueryWrapper<UserEntity> wrapper = buildUniqueKeyQuery(UserEntity::getEmployeeNo,
					form.getEmployeeNo(), excludeUserId);
			BusinessKeyAssert.requireAbsent(sysUserMapper, wrapper,
					() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "工号",
							form.getEmployeeNo()));
		}
	}

	/**
	 * 校验目标用户是否允许当前写操作（超级管理员与当前登录用户不可被操作）
	 * @param userIds 待操作用户主键
	 */
	public void requireOperable(Collection<Long> userIds) {
		Long currentUserId = SecurityUserUtils.getUserId();

		for (Long userId : userIds) {
			boolean protectedTarget = PermissionConstant.isSuperAdmin(userId)
					|| (currentUserId != null && currentUserId.equals(userId));

			if (protectedTarget) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN, userId);
			}
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
