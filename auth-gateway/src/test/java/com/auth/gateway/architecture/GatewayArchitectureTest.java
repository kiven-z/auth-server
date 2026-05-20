package com.auth.gateway.architecture;

import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * auth-gateway 模块架构约束（ArchUnit）。
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.gateway", importOptions = ImportOption.DoNotIncludeTests.class)
class GatewayArchitectureTest {

	private static final String GATEWAY_ROOT = "..gateway..";

	@ArchTest
	static final ArchRule business_exception_handlers_must_not_extend_validation_handler = noClasses().that()
		.resideInAPackage("..exception..")
		.and()
		.areAnnotatedWith(RestControllerAdvice.class)
		.should()
		.beAssignableTo(ValidationExceptionHandler.class);

	@ArchTest
	static final ArchRule must_not_declare_rest_template = noClasses().that()
		.resideInAPackage(GATEWAY_ROOT)
		.should()
		.dependOnClassesThat()
		.areAssignableTo(RestTemplate.class);

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(GATEWAY_ROOT)
		.should()
		.haveName("printStackTrace");

}
