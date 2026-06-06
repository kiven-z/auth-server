package com.auth.service.system.admin.support.tree;

import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TreeParentIdNormalizer} 单元测试。
 */
@DisplayName("TreeParentIdNormalizer 父节点 ID 规范化")
class TreeParentIdNormalizerTest {

	@Test
	@DisplayName("normalize：null 与负数归一为顶级")
	void normalizeMapsNullAndNegativeToRoot() {
		assertThat(TreeParentIdNormalizer.normalize(null)).isZero();
		assertThat(TreeParentIdNormalizer.normalize(-1L)).isZero();
		assertThat(TreeParentIdNormalizer.normalize(5L)).isEqualTo(5L);
	}

}
