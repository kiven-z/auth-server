package com.auth.service.system.admin.support.user;

import com.auth.common.data.model.enums.UserStatus;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link UserOperationChecker} 单元测试
 *
 * @author Bunny
 */
@DisplayName("UserOperationChecker 用户写操作校验")
@ExtendWith(MockitoExtension.class)
class UserOperationCheckerTest {

	@InjectMocks
	private UserOperationChecker userOperationChecker;

	@Test
	@DisplayName("删除：包含超级管理员时拒绝")
	void requireOperableThrowsWhenSuperAdminIncludedOnDelete() {
		assertThatThrownBy(() -> userOperationChecker.requireOperable(List.of(1L, 2L), UserProtectedOperation.DELETE))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
	}

	@Test
	@DisplayName("删除：包含当前登录用户时拒绝")
	void requireOperableThrowsWhenDeletingSelf() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(8L);

			assertThatThrownBy(() -> userOperationChecker.requireOperable(List.of(8L), UserProtectedOperation.DELETE))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		}
	}

	@Test
	@DisplayName("删除：校验通过时不抛异常")
	void requireOperablePassesForNormalUsersOnDelete() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(99L);

			assertThatCode(() -> userOperationChecker.requireOperable(List.of(2L, 3L), UserProtectedOperation.DELETE))
				.doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("改状态：包含超级管理员时拒绝")
	void requireOperableThrowsWhenSuperAdminIncludedOnStatusChange() {
		assertThatThrownBy(
				() -> userOperationChecker.requireOperable(List.of(1L, 2L), UserProtectedOperation.STATUS_DISABLED))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
	}

	@Test
	@DisplayName("改状态：禁用或锁定当前登录用户时拒绝")
	void requireOperableThrowsWhenDisablingOrLockingSelf() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(8L);

			assertThatThrownBy(
					() -> userOperationChecker.requireOperable(List.of(8L), UserProtectedOperation.STATUS_DISABLED))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

			assertThatThrownBy(
					() -> userOperationChecker.requireOperable(List.of(8L), UserProtectedOperation.STATUS_LOCKED))
				.isInstanceOf(SystemBusinessException.class)
				.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
				.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
		}
	}

	@Test
	@DisplayName("改状态：启用当前登录用户时允许")
	void requireOperableAllowsEnablingSelf() {
		try (MockedStatic<SecurityUserUtils> securityUserUtils = mockStatic(SecurityUserUtils.class)) {
			securityUserUtils.when(SecurityUserUtils::getUserId).thenReturn(8L);

			assertThatCode(
					() -> userOperationChecker.requireOperable(List.of(8L), UserProtectedOperation.STATUS_NORMAL))
				.doesNotThrowAnyException();
		}
	}

	@Test
	@DisplayName("改状态：根据状态码解析写操作类型")
	void fromStatusMapsUserStatusToOperation() {
		assertThat(UserProtectedOperation.fromStatus(UserStatus.NORMAL.getCode()))
			.isEqualTo(UserProtectedOperation.STATUS_NORMAL);
		assertThat(UserProtectedOperation.fromStatus(UserStatus.DISABLED.getCode()))
			.isEqualTo(UserProtectedOperation.STATUS_DISABLED);
		assertThat(UserProtectedOperation.fromStatus(UserStatus.LOCKED.getCode()))
			.isEqualTo(UserProtectedOperation.STATUS_LOCKED);
	}

}
