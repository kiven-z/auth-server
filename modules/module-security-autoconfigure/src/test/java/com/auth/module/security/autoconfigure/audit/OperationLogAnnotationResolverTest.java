package com.auth.module.security.autoconfigure.audit;

import com.auth.module.security.autoconfigure.audit.operationlog.OperationLogAnnotationResolver;
import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.AuditServiceDomain;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link OperationLogAnnotationResolver} 单元测试
 *
 * @author Bunny
 */
@DisplayName("OperationLogAnnotationResolver")
@ExtendWith(MockitoExtension.class)
class OperationLogAnnotationResolverTest {

	private static final String BIZ_DEPT = "SYS_DEPT";

	private static final String BIZ_USER = "SYS_USER";

	private static final String BIZ_MENU = "SYS_MENU";

	private static final String BIZ_ROLE = "SYS_ROLE";

	@Mock
	private ProceedingJoinPoint joinPoint;

	@Mock
	private MethodSignature methodSignature;

	@Test
	@DisplayName("应解析方法上的 OperationLog")
	void resolve_shouldReadMethodAnnotation() throws Exception {
		MethodAnnotatedController target = new MethodAnnotatedController();
		Method method = MethodAnnotatedController.class.getMethod("query");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAnnotationResolver.resolve(joinPoint);

		assertNotNull(meta);
		assertEquals(AuditServiceDomain.SYSTEM, meta.serviceDomain());
		assertEquals(BIZ_DEPT, meta.bizModule());
		assertEquals(OperationLogKind.QUERY, meta.operation());
		assertEquals("DEPT", meta.targetType());
	}

	@Test
	@DisplayName("方法无注解时应回退到类上的 OperationLog")
	void resolve_shouldReadClassAnnotationWhenMethodBare() throws Exception {
		ClassAnnotatedController target = new ClassAnnotatedController();
		Method method = ClassAnnotatedController.class.getMethod("plain");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAnnotationResolver.resolve(joinPoint);

		assertNotNull(meta);
		assertEquals(AuditServiceDomain.SYSTEM, meta.serviceDomain());
		assertEquals(BIZ_USER, meta.bizModule());
		assertEquals(OperationLogKind.UPDATE, meta.operation());
	}

	@Test
	@DisplayName("方法与类均有注解时以方法为准")
	void resolve_shouldPreferMethodOverClass() throws Exception {
		OverridingController target = new OverridingController();
		Method method = OverridingController.class.getMethod("write");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAnnotationResolver.resolve(joinPoint);

		assertNotNull(meta);
		assertEquals(AuditServiceDomain.SYSTEM, meta.serviceDomain());
		assertEquals(BIZ_ROLE, meta.bizModule());
		assertEquals(OperationLogKind.DELETE, meta.operation());
	}

	@Test
	@DisplayName("目标为 null 时应仍能解析声明类上的方法注解")
	void resolve_shouldWorkWhenTargetNull() throws Exception {
		Method method = MethodAnnotatedController.class.getMethod("query");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(null);

		OperationLog meta = OperationLogAnnotationResolver.resolve(joinPoint);

		assertNotNull(meta);
		assertEquals(BIZ_DEPT, meta.bizModule());
	}

	@Test
	@DisplayName("无任何 OperationLog 时应返回 null")
	void resolve_shouldReturnNullWhenAbsent() throws Exception {
		class NoLog {

			@SuppressWarnings("unused")
			public void x() {
			}

		}
		NoLog target = new NoLog();
		Method method = NoLog.class.getMethod("x");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAnnotationResolver.resolve(joinPoint);

		assertNull(meta);
	}

	static class MethodAnnotatedController {

		@OperationLog(serviceDomain = AuditServiceDomain.SYSTEM, bizModule = BIZ_DEPT, targetType = "DEPT",
				operation = OperationLogKind.QUERY)
		public void query() {
		}

	}

	@OperationLog(serviceDomain = AuditServiceDomain.SYSTEM, bizModule = BIZ_USER, targetType = "",
			operation = OperationLogKind.UPDATE)
	static class ClassAnnotatedController {

		public void plain() {
		}

	}

	@OperationLog(serviceDomain = AuditServiceDomain.SYSTEM, bizModule = BIZ_MENU, targetType = "",
			operation = OperationLogKind.QUERY)
	static class OverridingController {

		@OperationLog(serviceDomain = AuditServiceDomain.SYSTEM, bizModule = BIZ_ROLE, targetType = "",
				operation = OperationLogKind.DELETE)
		public void write() {
		}

	}

}
