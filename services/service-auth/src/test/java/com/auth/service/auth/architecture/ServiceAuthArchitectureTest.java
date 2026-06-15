package com.auth.service.auth.architecture;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * service-auth 模块架构约束（ArchUnit）。
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
class ServiceAuthArchitectureTest {

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage("..api.controller..")
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

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
	static final ArchRule authorization_must_not_depend_on_authentication_domain = noClasses().that()
		.resideInAPackage("..application.authorization..")
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..domain.authentication..");

	@ArchTest
	static final ArchRule authorization_application_must_not_use_user_entity = noClasses().that()
		.resideInAPackage("..application.authorization..")
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

	private static final String AUTH_ROOT = "..service.auth..";

	@ArchTest
	static final ArchRule must_not_depend_on_system_implementation = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.auth.service.system..");

	@ArchTest
	static final ArchRule no_printStackTrace = noMethods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.haveName("printStackTrace");

	@ArchTest
	static final ArchRule must_not_declare_rest_template = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.areAssignableTo(RestTemplate.class);

	private static final String AUTH_LOGIN_APPLICATION = "..application.login..";

	private static final String AUTH_APPLICATION = "..application..";

	private static final String AUTH_SESSION_APPLICATION = "..application.session..";

	private static final String AUTH_AUTHORIZATION_APPLICATION = "..application.authorization..";

	@ArchTest
	static final ArchRule application_must_not_depend_on_infrastructure = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..infrastructure..");

	/**
	 * session / authorization 已 Port 化，禁止反向依赖本模块 api（DTO 映射留在 Controller + convert）。 不含
	 * {@code module-security-contract} 的 {@code ..contract.api..} 包。
	 * {@code application.login} 仍使用 api.dto 响应类型，待 login 读模型拆分后再收紧全局规则。
	 */
	@ArchTest
	static final ArchRule session_application_must_not_depend_on_api = noClasses().that()
		.resideInAPackage(AUTH_SESSION_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.auth.service.auth.api..");

	@ArchTest
	static final ArchRule authorization_application_must_not_depend_on_api = noClasses().that()
		.resideInAPackage(AUTH_AUTHORIZATION_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.auth.service.auth.api..");

	@ArchTest
	static final ArchRule authentication_application_must_not_depend_on_authorization_application = noClasses().that()
		.resideInAPackage(AUTH_LOGIN_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..application.authorization..");

	@ArchTest
	static final ArchRule authentication_application_must_not_depend_on_infrastructure = noClasses().that()
		.resideInAPackage(AUTH_LOGIN_APPLICATION)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..infrastructure..");

}