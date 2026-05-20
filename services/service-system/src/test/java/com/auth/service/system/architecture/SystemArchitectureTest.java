package com.auth.service.system.architecture;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * service-system 模块架构约束（ArchUnit）。
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.system", importOptions = ImportOption.DoNotIncludeTests.class)
class SystemArchitectureTest {

	private static final String SYSTEM_ROOT = "..service.system..";

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
		.should()
		.haveRawReturnType(Result.class);

}
