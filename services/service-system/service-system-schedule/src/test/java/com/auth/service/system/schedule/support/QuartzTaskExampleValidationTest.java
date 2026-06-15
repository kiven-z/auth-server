package com.auth.service.system.schedule.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.support.builtin.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob;
import com.auth.service.system.schedule.task.builtin.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 内置 {@link BuiltinQuartzJob} 的 {@link QuartzTask#example()} 应与载荷校验规则一致。
 *
 * @author Bunny
 */
@DisplayName("QuartzTask example 与 job_params 校验一致性")
class QuartzTaskExampleValidationTest {

	private static BuiltinJobParamsParser parser;

	@BeforeAll
	static void setUpParser() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		parser = new BuiltinJobParamsParser(new ObjectMapper(), beanValidator);
	}

	static Stream<Arguments> builtinJobExamples() {
		return Stream.of(Arguments.of(HttpInvokeJob.class, HttpInvokeJobParams.class),
				Arguments.of(FeignInvokeJob.class, FeignInvokeJobParams.class));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("builtinJobExamples")
	@DisplayName("@QuartzTask example 应能通过 parseAndValidate")
	void quartzTaskExample_passesValidation(Class<?> jobClass, Class<?> paramsClass) {
		QuartzTask ann = AnnotationUtils.findAnnotation(jobClass, QuartzTask.class);
		String example = CharSequenceUtil.trim(ann.example());

		assertThatCode(() -> parser.parseAndValidate(example, paramsClass)).doesNotThrowAnyException();
	}

}
