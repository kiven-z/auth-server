package com.auth.service.auth.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static com.auth.service.auth.architecture.ServiceAuthArchitectureConditions.ARE_QUERY_SERVICES;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * service-auth 目标架构约束（ArchUnit）。
 * <p>
 * 规则对应当前目录划分规范，与 {@link ServiceAuthArchitectureTest} 一同在常规测试套件中执行。
 * </p>
 *
 * <h2>架构约束分组</h2>
 * <ul>
 * <li>MIGRATION-1：domain 无 po/vo/query、无 OpenAPI</li>
 * <li>MIGRATION-2：Controller 不依赖 domain 展示类型</li>
 * <li>MIGRATION-3：Port 不泄漏 MyBatis 与 PO</li>
 * <li>MIGRATION-4：Port 实现归 infrastructure adapter</li>
 * <li>MIGRATION-5：MyBatis Mapper 收拢至 persistence.mapper</li>
 * <li>MIGRATION-6：application.login 不依赖 api.dto（Command 侧；读路径见 CQRS-READ）</li>
 * <li>MIGRATION-7：Feign 归 infrastructure.integration</li>
 * <li>CQRS-READ：QueryService 允许 Mapper 与 persistence；Command 禁止</li>
 * </ul>
 *
 * @author Bunny
 */
@AnalyzeClasses(packages = "com.auth.service.auth", importOptions = ImportOption.DoNotIncludeTests.class)
@Tag("architecture-target")
@DisplayName("service-auth 目标架构约束")
class ServiceAuthArchitectureTargetTest {

	// --- MIGRATION-1 domain 纯净 ---

	@ArchTest
	static final ArchRule domain_must_not_contain_vo_packages = noClasses().should()
		.resideInAnyPackage("..domain..vo..", "..domain..model.vo..");

	@ArchTest
	static final ArchRule domain_must_not_contain_po_packages = noClasses().should()
		.resideInAnyPackage("..domain..po..", "..domain..model.po..");

	@ArchTest
	static final ArchRule domain_must_not_contain_query_packages = noClasses().should()
		.resideInAnyPackage("..domain..query..", "..domain..model.query..");

	@ArchTest
	static final ArchRule domain_must_not_depend_on_openapi = noClasses().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_DOMAIN)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("io.swagger..");

	// --- MIGRATION-2 api 契约 ---

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_domain_vo = noClasses().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_API_CONTROLLER)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..domain..vo..", "..domain..model.vo..");

	@ArchTest
	static final ArchRule controllers_must_not_depend_on_domain_query = noClasses().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_API_CONTROLLER)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..domain..query..", "..domain..model.query..");

	// --- MIGRATION-3 Port 契约 ---

	@ArchTest
	static final ArchRule ports_must_not_depend_on_mybatis = noClasses().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_APPLICATION_PORT)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("com.baomidou.mybatisplus..");

	@ArchTest
	static final ArchRule ports_must_not_depend_on_domain_po = noClasses().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_APPLICATION_PORT)
		.should()
		.dependOnClassesThat()
		.resideInAnyPackage("..domain..model.po..", "..domain..po..");

	// --- MIGRATION-4 基础设施 adapter ---

	@ArchTest
	static final ArchRule application_must_not_implement_ports = classes().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_APPLICATION)
		.and()
		.areNotInterfaces()
		.should(ServiceAuthArchitectureConditions.notImplementApplicationPort());

	@ArchTest
	static final ArchRule port_implementations_must_reside_in_infrastructure = classes().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_ROOT)
		.should(ServiceAuthArchitectureConditions.implementApplicationPortsOnlyInInfrastructure());

	@ArchTest
	static final ArchRule port_adapters_must_reside_in_adapter_packages = classes().that()
		.resideInAPackage(ServiceAuthArchitecturePackages.AUTH_INFRASTRUCTURE)
		.should(ServiceAuthArchitectureConditions.resideInInfrastructureAdapterPackageWhenPortAdapter());

	// --- MIGRATION-5 持久化 Mapper ---

	@ArchTest
	static final ArchRule mybatis_mappers_must_reside_in_mapper_package = classes().that()
		.haveSimpleNameEndingWith("Mapper")
		.and()
		.areInterfaces()
		.should()
		.resideInAPackage("..infrastructure.persistence.mapper..");

	// --- CQRS-READ QueryService 包约定 ---

	@ArchTest
	static final ArchRule query_services_should_reside_in_application_query_package = classes().that(ARE_QUERY_SERVICES)
		.should()
		.resideInAnyPackage(ServiceAuthArchitecturePackages.AUTH_APPLICATION_QUERY);

	@ArchTest
	static final ArchRule query_services_must_not_implement_ports = classes().that(ARE_QUERY_SERVICES)
		.should(ServiceAuthArchitectureConditions.notImplementApplicationPort());

	// --- MIGRATION-7 出站 Feign ---

	@ArchTest
	static final ArchRule feign_clients_must_reside_in_integration = classes().that()
		.areAnnotatedWith(org.springframework.cloud.openfeign.FeignClient.class)
		.should()
		.resideInAPackage("..infrastructure.integration..");

}
