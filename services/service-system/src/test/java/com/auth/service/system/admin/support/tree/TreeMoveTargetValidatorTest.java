package com.auth.service.system.admin.support.tree;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TreeMoveTargetValidator} 单元测试。
 */
@DisplayName("TreeMoveTargetValidator 树移动目标校验")
class TreeMoveTargetValidatorTest {

	private static final SystemBusinessException SELF = new SystemBusinessException(
			SystemAdminResultCode.DEPT_MOVE_TO_SELF, 5L);

	private static final SystemBusinessException DESCENDANT = new SystemBusinessException(
			SystemAdminResultCode.DEPT_MOVE_TO_DESCENDANT, 10L, 20L);

	@Test
	@DisplayName("requireValid：不能移动到自身")
	void requireValidRejectsSelf() {
		assertThatThrownBy(() -> TreeMoveTargetValidator.requireValid(5L, 5L, (a, n) -> false, SELF, DESCENDANT))
			.isSameAs(SELF);
	}

	@Test
	@DisplayName("requireValid：不能移动到后代")
	void requireValidRejectsDescendant() {
		assertThatThrownBy(() -> TreeMoveTargetValidator.requireValid(10L, 20L,
				(a, n) -> a.equals(10L) && n.equals(20L), SELF, DESCENDANT))
			.isSameAs(DESCENDANT);
	}

	@Test
	@DisplayName("requireValid：合法目标不抛异常")
	void requireValidAcceptsValidTarget() {
		assertThatCode(() -> TreeMoveTargetValidator.requireValid(10L, 0L, (a, n) -> false, SELF, DESCENDANT))
			.doesNotThrowAnyException();
	}

}
