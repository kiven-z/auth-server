package com.auth.module.security.autoconfigure.audit;

import com.auth.module.security.contract.annotation.OperationLog;
import com.auth.module.security.contract.api.audit.OperationLogKind;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link OperationLogAspect} 注解解析单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OperationLogAspect 注解解析")
class OperationLogAspectResolveTest {

	@Mock
	private ProceedingJoinPoint joinPoint;

	@Mock
	private MethodSignature methodSignature;

	@Test
	@DisplayName("应解析方法上的 OperationLog")
	void resolveDeclaredOperationLog_shouldReadMethodAnnotation() throws Exception {
		MethodAnnotatedController target = new MethodAnnotatedController();
		Method method = MethodAnnotatedController.class.getMethod("query");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAspect.resolveDeclaredOperationLog(joinPoint);

		assertNotNull(meta);
		assertEquals("DEPT", meta.module());
		assertEquals(OperationLogKind.QUERY, meta.operation());
		assertEquals("DEPT", meta.targetType());
	}

	@Test
	@DisplayName("方法无注解时应回退到类上的 OperationLog")
	void resolveDeclaredOperationLog_shouldReadClassAnnotationWhenMethodBare() throws Exception {
		ClassAnnotatedController target = new ClassAnnotatedController();
		Method method = ClassAnnotatedController.class.getMethod("plain");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAspect.resolveDeclaredOperationLog(joinPoint);

		assertNotNull(meta);
		assertEquals("SYS", meta.module());
		assertEquals(OperationLogKind.UPDATE, meta.operation());
	}

	@Test
	@DisplayName("方法与类均有注解时以方法为准")
	void resolveDeclaredOperationLog_shouldPreferMethodOverClass() throws Exception {
		OverridingController target = new OverridingController();
		Method method = OverridingController.class.getMethod("write");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(target);

		OperationLog meta = OperationLogAspect.resolveDeclaredOperationLog(joinPoint);

		assertNotNull(meta);
		assertEquals("METHOD", meta.module());
		assertEquals(OperationLogKind.DELETE, meta.operation());
	}

	@Test
	@DisplayName("目标为 null 时应仍能解析声明类上的方法注解")
	void resolveDeclaredOperationLog_shouldWorkWhenTargetNull() throws Exception {
		Method method = MethodAnnotatedController.class.getMethod("query");
		when(joinPoint.getSignature()).thenReturn(methodSignature);
		when(methodSignature.getMethod()).thenReturn(method);
		when(joinPoint.getTarget()).thenReturn(null);

		OperationLog meta = OperationLogAspect.resolveDeclaredOperationLog(joinPoint);

		assertNotNull(meta);
		assertEquals("DEPT", meta.module());
	}

	@Test
	@DisplayName("无任何 OperationLog 时应返回 null")
	void resolveDeclaredOperationLog_shouldReturnNullWhenAbsent() throws Exception {
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

		OperationLog meta = OperationLogAspect.resolveDeclaredOperationLog(joinPoint);

		assertNull(meta);
	}

	static class MethodAnnotatedController {

		@OperationLog(module = "DEPT", targetType = "DEPT", operation = OperationLogKind.QUERY)
		public void query() {
		}

	}

	@OperationLog(module = "SYS", targetType = "", operation = OperationLogKind.UPDATE)
	static class ClassAnnotatedController {

		public void plain() {
		}

	}

	@OperationLog(module = "CLASS", targetType = "", operation = OperationLogKind.QUERY)
	static class OverridingController {

		@OperationLog(module = "METHOD", targetType = "", operation = OperationLogKind.DELETE)
		public void write() {
		}

	}

}
