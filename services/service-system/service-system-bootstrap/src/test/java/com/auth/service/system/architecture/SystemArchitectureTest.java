package com.auth.service.system.architecture;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * service-system 模块化单体架构约束（ArchUnit）。
 * <p>
 * 扫描 classpath 上全部 {@code com.auth.service.system} 域代码（含子模块 jar）。
 * </p>
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.system", importOptions = ImportOption.DoNotIncludeTests.class)
class SystemArchitectureTest {

	private static final String SYSTEM_ROOT = "..service.system..";

	private static final String ADMIN = "com.auth.service.system.admin..";

	private static final String SCHEDULE = "com.auth.service.system.schedule..";

	private static final String MESSAGE = "com.auth.service.system.message..";

	private static final String AUTHORIZATION = "com.auth.service.system.authorization..";

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage("..controller..")
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	@ArchTest
	static final ArchRule business_exception_handlers_must_not_extend_validation_handler = noClasses().that()
		.resideInAPackage("..common.exception..")
		.and()
		.areAnnotatedWith(RestControllerAdvice.class)
		.should()
		.beAssignableTo(ValidationExceptionHandler.class);

	/**
	 * 允许通过 Starter 注入 {@link RestTemplate}，禁止在模块内 {@code new RestTemplate()} 重复造轮子。
	 */
	@ArchTest
	static final ArchRule must_not_instantiate_rest_template = noClasses().that()
		.resideInAPackage(SYSTEM_ROOT)
		.should()
		.callConstructor(RestTemplate.class);

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(SYSTEM_ROOT)
		.should()
		.haveName("printStackTrace");

	/**
	 * 常规 JSON API 返回 {@link Result}；二进制文件下载（如 Excel 导出）允许 {@link ResponseEntity}。
	 */
	@ArchTest
	static final ArchRule controller_public_api_methods_return_result = methods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage("..controller..")
		.and()
		.arePublic()
		.and()
		.doNotHaveName("equals")
		.and()
		.doNotHaveName("hashCode")
		.and()
		.doNotHaveName("toString")
		.and()
		.doNotHaveRawReturnType(ResponseEntity.class)
		.should()
		.haveRawReturnType(Result.class);

	@ArchTest
	static final ArchRule admin_must_not_depend_on_message_or_schedule = noClasses().that()
		.resideInAPackage(ADMIN)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(MESSAGE, SCHEDULE);

	@ArchTest
	static final ArchRule message_must_not_depend_on_other_domains = noClasses().that()
		.resideInAPackage(MESSAGE)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(ADMIN, SCHEDULE, AUTHORIZATION);

	@ArchTest
	static final ArchRule authorization_must_not_depend_on_admin = noClasses().that()
		.resideInAPackage(AUTHORIZATION)
		.should()
		.dependOnClassesThat()
		.resideInAPackage(ADMIN);

	/**
	 * schedule 调用 message 时只允许依赖 {@code message.service} 包（Maven 模块依赖已约束模块级边界）。
	 */
	@ArchTest
	static final ArchRule schedule_must_not_depend_on_message_internals = noClasses().that()
		.resideInAPackage(SCHEDULE)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.auth.service.system.message.controller..", "com.auth.service.system.message.channel..",
				"com.auth.service.system.message.config..", "com.auth.service.system.message.convert..",
				"com.auth.service.system.message.mapper..", "com.auth.service.system.message.model..",
				"com.auth.service.system.message.template..", "com.auth.service.system.message.validation..");

	@ArchTest
	static final ArchRule message_and_schedule_must_not_use_feign_client = noClasses().that()
		.resideInAnyPackage(MESSAGE, SCHEDULE)
		.should()
		.dependOnClassesThat()
		.resideInAPackage("com.auth.service.system.common.feign..");

}
