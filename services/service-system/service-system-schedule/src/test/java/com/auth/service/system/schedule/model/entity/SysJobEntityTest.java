package com.auth.service.system.schedule.model.entity;

import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobEntity} 持久化元数据校验。
 */
@DisplayName("SysJobEntity 持久化元数据")
class SysJobEntityTest {

	@Test
	@DisplayName("startTime/endTime 使用 ALWAYS 更新策略以支持清空")
	void scheduleWindowFieldsUseAlwaysUpdateStrategy() throws NoSuchFieldException {
		Field startTimeField = SysJobEntity.class.getDeclaredField("startTime");
		Field endTimeField = SysJobEntity.class.getDeclaredField("endTime");
		TableField startAnnotation = startTimeField.getAnnotation(TableField.class);
		TableField endAnnotation = endTimeField.getAnnotation(TableField.class);

		assertThat(startAnnotation.updateStrategy()).isEqualTo(FieldStrategy.ALWAYS);
		assertThat(endAnnotation.updateStrategy()).isEqualTo(FieldStrategy.ALWAYS);
	}

}
