package com.auth.service.auth.architecture;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestTemplate;

import static com.auth.service.auth.architecture.ServiceAuthArchitectureConditions.ARE_COMMAND_APPLICATION_CLASSES;
import static com.auth.service.auth.architecture.ServiceAuthArchitectureConditions.ARE_NOT_QUERY_SERVICES;
import static com.auth.service.auth.architecture.ServiceAuthArchitectureConditions.ARE_QUERY_SERVICES;
import static com.auth.service.auth.architecture.ServiceAuthArchitecturePackages.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * service-auth 模块架构约束（ArchUnit）。
 * <p>
 * 本类包含当前代码库已满足的「底线规则」；CQRS 读路径例外见
 * {@link ServiceAuthArchitectureConditions#ARE_QUERY_SERVICES}。 目标态规则见
 * {@link ServiceAuthArchitectureTargetTest}。
 * </p>
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("service-auth 架构约束（已生效）")
class ServiceAuthArchitectureTest {

	// --- api 层 ---

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage(AUTH_API_CONTROLLER)
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_infrastructure = noClasses().that()
		.resideInAPackage(AUTH_API_CONTROLLER)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_INFRASTRUCTURE);

	@ArchTest
	static final ArchRule controller_public_api_methods_return_result = methods().that()
		.areDeclaredInClassesThat()
		.resideInAPackage(AUTH_API_CONTROLLER)
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

	// --- application 层（Command 与 Query 分轨，见 auth 读路径 CQRS 规范） ---

	@ArchTest
	static final ArchRule application_command_must_not_depend_on_infrastructure_except_projection = noClasses()
		.that(ARE_COMMAND_APPLICATION_CLASSES)
		.should()
		.dependOnClassesThat(
				resideInAnyPackage(AUTH_INFRASTRUCTURE).and(resideOutsideOfPackage(AUTH_INFRASTRUCTURE_PROJECTION)));

	@ArchTest
	static final ArchRule application_query_must_not_depend_on_infrastructure_except_persistence = noClasses()
		.that(ARE_QUERY_SERVICES)
		.should()
		.dependOnClassesThat(
				resideInAnyPackage(AUTH_INFRASTRUCTURE).and(resideOutsideOfPackage(AUTH_INFRASTRUCTURE_PERSISTENCE)));

	@ArchTest
	static final ArchRule port_must_not_depend_on_infrastructure_except_projection = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_PORT)
		.should()
		.dependOnClassesThat(
				resideInAnyPackage(AUTH_INFRASTRUCTURE).and(resideOutsideOfPackage(AUTH_INFRASTRUCTURE_PROJECTION)));

	@ArchTest
	static final ArchRule convert_must_not_depend_on_infrastructure_except_projection = noClasses().that()
		.resideInAPackage(AUTH_CONVERT)
		.should()
		.dependOnClassesThat(
				resideInAnyPackage(AUTH_INFRASTRUCTURE).and(resideOutsideOfPackage(AUTH_INFRASTRUCTURE_PROJECTION)));

	@ArchTest
	static final ArchRule application_command_must_not_depend_on_mappers = noClasses()
		.that(ARE_COMMAND_APPLICATION_CLASSES)
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	@ArchTest
	static final ArchRule session_application_must_not_depend_on_api = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_SESSION)
		.and(ARE_NOT_QUERY_SERVICES)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_HTTP_API);

	@ArchTest
	static final ArchRule authorization_application_must_not_depend_on_api = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_AUTHORIZATION)
		.and(ARE_NOT_QUERY_SERVICES)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_HTTP_API);

	@ArchTest
	static final ArchRule login_application_must_not_depend_on_api = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_LOGIN)
		.and(ARE_NOT_QUERY_SERVICES)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_HTTP_API);

	@ArchTest
	static final ArchRule login_application_must_not_depend_on_authorization_application = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_LOGIN)
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..application.authorization..");

	@ArchTest
	static final ArchRule authorization_must_not_depend_on_authentication_domain = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_AUTHORIZATION)
		.should()
		.dependOnClassesThat()
		.resideInAPackage("..domain.authentication..");

	@ArchTest
	static final ArchRule authorization_application_must_not_use_user_entity = noClasses().that()
		.resideInAPackage(AUTH_APPLICATION_AUTHORIZATION)
		.should()
		.dependOnClassesThat()
		.haveFullyQualifiedName("com.auth.module.platform.persistence.model.UserEntity");

	@ArchTest
	static final ArchRule concrete_login_strategies_reside_in_application_layer = classes().that()
		.haveSimpleNameEndingWith("LoginStrategy")
		.and()
		.areNotInterfaces()
		.should()
		.resideInAPackage("..application.login.strategy..");

	@ArchTest
	static final ArchRule port_interfaces_must_reside_in_application_port = classes().that()
		.areInterfaces()
		.and()
		.haveSimpleNameEndingWith("Port")
		.should()
		.resideInAPackage(AUTH_APPLICATION_PORT);

	// --- domain 层 ---

	@ArchTest
	static final ArchRule domain_must_not_depend_on_api = noClasses().that()
		.resideInAPackage(AUTH_DOMAIN)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_HTTP_API);

	@ArchTest
	static final ArchRule domain_must_not_depend_on_application = noClasses().that()
		.resideInAPackage(AUTH_DOMAIN)
		.should()
		.dependOnClassesThat()
		.resideInAPackage(AUTH_APPLICATION);

	@ArchTest
	static final ArchRule domain_must_not_depend_on_infrastructure = noClasses().that()
		.resideInAPackage(AUTH_DOMAIN)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_INFRASTRUCTURE);

	@ArchTest
	static final ArchRule domain_must_not_depend_on_mybatis = noClasses().that()
		.resideInAPackage(AUTH_DOMAIN)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.baomidou.mybatisplus..");

	// --- convert / job ---

	@ArchTest
	static final ArchRule mapstruct_mappers_must_reside_in_convert = classes().that()
		.areAnnotatedWith(Mapper.class)
		.should()
		.resideInAPackage(AUTH_CONVERT);

	@ArchTest
	static final ArchRule job_must_not_depend_on_infrastructure = noClasses().that()
		.resideInAPackage(AUTH_JOB)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(AUTH_INFRASTRUCTURE);

	@ArchTest
	static final ArchRule job_must_not_depend_on_mappers = noClasses().that()
		.resideInAPackage(AUTH_JOB)
		.should()
		.dependOnClassesThat()
		.haveSimpleNameEndingWith("Mapper");

	// --- 模块级横切 ---

	@ArchTest
	static final ArchRule auth_exception_handlers_must_not_extend_validation_handler = noClasses().that()
		.resideInAPackage("..shared.exception..")
		.and()
		.areAnnotatedWith(RestControllerAdvice.class)
		.should()
		.beAssignableTo(ValidationExceptionHandler.class);

	@ArchTest
	static final ArchRule must_not_depend_on_system_implementation = noClasses().that()
		.resideInAPackage(AUTH_ROOT)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage(SYSTEM_IMPLEMENTATION);

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

}
