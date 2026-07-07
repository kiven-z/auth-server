package com.auth.service.system.schedule.config;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ScheduleJobProperties} 字段约束校验测试。
 *
 * @author Bunny
 */
@DisplayName("ScheduleJobProperties 配置校验")
class ScheduleJobPropertiesValidationTest {

	private static Validator validator;

	@BeforeAll
	static void initValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private static ScheduleJobProperties validProperties() {
		ScheduleJobProperties properties = new ScheduleJobProperties();
		properties.setConsecutiveFailureThreshold(5);
		properties.setBootstrapBatchSize(100);
		properties.setSuccessEnabled(false);
		return properties;
	}

	@Test
	@DisplayName("默认值应通过校验")
	void defaultValues_areValid() {
		Set<ConstraintViolation<ScheduleJobProperties>> violations = validator.validate(new ScheduleJobProperties());

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("consecutiveFailureThreshold 为 0 时应通过校验（关闭熔断）")
	void consecutiveFailureThreshold_zero_isValid() {
		ScheduleJobProperties properties = validProperties();
		properties.setConsecutiveFailureThreshold(0);

		Set<ConstraintViolation<ScheduleJobProperties>> violations = validator.validate(properties);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("consecutiveFailureThreshold 小于 0 时应校验失败")
	void consecutiveFailureThreshold_negative_isInvalid() {
		ScheduleJobProperties properties = validProperties();
		properties.setConsecutiveFailureThreshold(-1);

		Set<ConstraintViolation<ScheduleJobProperties>> violations = validator.validate(properties);

		assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
			.map(Object::toString)
			.containsExactly("consecutiveFailureThreshold");
	}

	@Test
	@DisplayName("bootstrapBatchSize 小于 10 时应校验失败")
	void bootstrapBatchSize_belowMinimum_isInvalid() {
		ScheduleJobProperties properties = validProperties();
		properties.setBootstrapBatchSize(9);

		Set<ConstraintViolation<ScheduleJobProperties>> violations = validator.validate(properties);

		assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
			.map(Object::toString)
			.containsExactly("bootstrapBatchSize");
	}

	@Test
	@DisplayName("bootstrapBatchSize 为 10 时应通过校验")
	void bootstrapBatchSize_atMinimum_isValid() {
		ScheduleJobProperties properties = validProperties();
		properties.setBootstrapBatchSize(10);

		Set<ConstraintViolation<ScheduleJobProperties>> violations = validator.validate(properties);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("successEnabled 为布尔值时不施加额外约束")
	void successEnabled_booleanValues_areValid() {
		ScheduleJobProperties enabled = validProperties();
		enabled.setSuccessEnabled(true);
		ScheduleJobProperties disabled = validProperties();
		disabled.setSuccessEnabled(false);

		assertThat(validator.validate(enabled)).isEmpty();
		assertThat(validator.validate(disabled)).isEmpty();
	}

}
