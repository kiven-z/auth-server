package com.auth.service.system.admin.support.tree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TreeParentChainDescendantChecker} 单元测试。
 */
@DisplayName("TreeParentChainDescendantChecker 父链后代判定")
class TreeParentChainDescendantCheckerTest {

	@Test
	@DisplayName("isDescendantOf：沿 parent 链识别后代")
	void isDescendantOfWalksParentChain() {
		Map<Long, Long> parents = Map.of(20L, 10L, 10L, 0L);

		boolean descendant = TreeParentChainDescendantChecker.isDescendantOf(10L, 20L,
				id -> parents.getOrDefault(id, null));

		assertThat(descendant).isTrue();
	}

	@Test
	@DisplayName("isDescendantOf：非后代返回 false")
	void isDescendantOfReturnsFalseForUnrelatedNode() {
		Map<Long, Long> parents = Map.of(20L, 30L, 30L, 0L);

		boolean descendant = TreeParentChainDescendantChecker.isDescendantOf(10L, 20L,
				id -> parents.getOrDefault(id, null));

		assertThat(descendant).isFalse();
	}

}
