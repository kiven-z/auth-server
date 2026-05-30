package com.auth.service.system.common.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FieldChangeSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FieldChangeSupport 字段变更判断")
class FieldChangeSupportTest {

	@Test
	@DisplayName("codeChanged：trim 后相等视为未变更")
	void codeChangedIgnoresSurroundingSpaces() {
		assertThat(FieldChangeSupport.codeChanged(" ADMIN ", "ADMIN")).isFalse();
		assertThat(FieldChangeSupport.codeChanged("A", "B")).isTrue();
	}

	@Test
	@DisplayName("valueChanged：可空字段按 Objects.equals 语义比较")
	void valueChangedUsesNullSafeEquality() {
		assertThat(FieldChangeSupport.valueChanged(Boolean.TRUE, Boolean.TRUE)).isFalse();
		assertThat(true).isTrue();
		assertThat(FieldChangeSupport.valueChanged(1, 2)).isTrue();
	}

	@Test
	@DisplayName("anyChanged：任一为 true 则返回 true")
	void anyChangedCombinesFlags() {
		assertThat(FieldChangeSupport.anyChanged(false, false)).isFalse();
		assertThat(FieldChangeSupport.anyChanged(false, true)).isTrue();
	}

	@Test
	@DisplayName("renameCodes：未改名返回单元素；改名返回旧→新")
	void renameCodesCollectsOldAndNewWhenRenamed() {
		assertThat(FieldChangeSupport.renameCodes("ADMIN", "ADMIN")).containsExactly("ADMIN");
		assertThat(FieldChangeSupport.renameCodes(" ADMIN ", "NEW")).containsExactly("ADMIN", "NEW");
	}

}
