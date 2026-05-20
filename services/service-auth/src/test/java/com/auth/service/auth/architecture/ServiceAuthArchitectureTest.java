package com.auth.service.auth.architecture;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * service-auth 模块架构约束（ArchUnit）。
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
class ServiceAuthArchitectureTest {

	private static final String AUTH_ROOT = "..service.auth..";

	private static final String AUTH_APPLICATION = "..authentication.application..";

	private static final String AUTH_INFRA = "..authentication.infrastructure..";

	private static final String SESSION_INFRA = "..session.infrastructure..";

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

	@ArchTest
	static final ArchRule authentication_application_must_not_depend_on_authorization_application = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..authorization.application..");

	@ArchTest
	static final ArchRule authentication_application_must_not_depend_on_infrastructure = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_INFRA, SESSION_INFRA);

	@ArchTest
	static final ArchRule must_not_declare_rest_template = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.areAssignableTo(RestTemplate.class);

	@ArchTest
	static final ArchRule authorization_must_not_depend_on_authentication_domain = noClasses().that()
		.resideInAPackage("..authorization..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..authentication.domain..");

	@ArchTest
	static final ArchRule authorization_application_must_not_use_user_entity = noClasses().that()
		.resideInAPackage("..authorization.application..")
		.should()
		.dependOnClassesThat()
		.haveFullyQualifiedName("com.auth.module.platform.persistence.model.UserEntity");

	@ArchTest
	static final ArchRule controller_public_api_methods_return_result = methods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage("..api.controller..")
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