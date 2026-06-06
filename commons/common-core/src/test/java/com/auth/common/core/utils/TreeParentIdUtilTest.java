package com.auth.common.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TreeParentIdUtil} 单元测试。
 */
@DisplayName("TreeParentIdUtil 父节点 ID 规范化")
class TreeParentIdUtilTest {

	@Test
	@DisplayName("normalize：null 与负数归一为顶级")
	void normalizeMapsNullAndNegativeToRoot() {
		// null 与负数均视为顶级
		assertThat(TreeParentIdUtil.normalize(null)).isZero();
		assertThat(TreeParentIdUtil.normalize(-1L)).isZero();
		assertThat(TreeParentIdUtil.normalize(5L)).isEqualTo(5L);
	}

}
