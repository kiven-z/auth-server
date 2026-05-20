package com.auth.service.auth.architecture;

import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * service-auth 模块架构约束（ArchUnit）。
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
class ServiceAuthArchitectureTest {

	private static final String AUTH_ROOT = "..service.auth..";

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage("..api.controller..")
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	@ArchTest
	static final ArchRule must_not_depend_on_system_implementation = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.auth.service.system..");

	@ArchTest
	static final ArchRule auth_exception_handlers_must_not_extend_validation_handler = noClasses().that()
		.resideInAPackage("..shared.exception..")
		.and()
		.areAnnotatedWith(RestControllerAdvice.class)
		.should()
		.beAssignableTo(ValidationExceptionHandler.class);

	@ArchTest
	static final ArchRule concrete_login_strategies_reside_in_application_layer = classes().that()
		.haveSimpleNameEndingWith("LoginStrategy")
		.and()
		.areNotInterfaces()
		.should()
		.resideInAPackage("..application.login.strategy..");

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.haveName("printStackTrace");

}
