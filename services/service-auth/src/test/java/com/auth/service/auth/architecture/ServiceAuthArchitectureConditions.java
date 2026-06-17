package com.auth.service.auth.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * service-auth 架构测试自定义条件。
 *
 * @author Bunny
 */
final class ServiceAuthArchitectureConditions {

	/**
	 * CQRS 读路径：类名以 QueryService 结尾，或位于 {@code application..query..} 包。
	 */
	static final DescribedPredicate<JavaClass> ARE_QUERY_SERVICES = new DescribedPredicate<>(
			"are QueryService classes") {
		@Override
		public boolean test(JavaClass javaClass) {
			if (javaClass.getSimpleName().endsWith("QueryService")) {
				return true;
			}
			return javaClass.getPackageName().contains(".application.")
					&& javaClass.getPackageName().contains(".query.");
		}
	};

	/**
	 * 非读路径 application 类（与 {@link #ARE_QUERY_SERVICES} 互斥）。
	 */
	static final DescribedPredicate<JavaClass> ARE_NOT_QUERY_SERVICES = new DescribedPredicate<>(
			"are not QueryService classes") {
		@Override
		public boolean test(JavaClass javaClass) {
			return !ARE_QUERY_SERVICES.test(javaClass);
		}
	};

	/**
	 * application 层中除 Port 接口与 QueryService 外的类（Command 编排、策略等）。
	 */
	static final DescribedPredicate<JavaClass> ARE_COMMAND_APPLICATION_CLASSES = new DescribedPredicate<>(
			"are command-side application classes") {
		@Override
		public boolean test(JavaClass javaClass) {
			String pkg = javaClass.getPackageName();
			if (!pkg.contains(".application.")) {
				return false;
			}
			if (pkg.contains(".application.port.")) {
				return false;
			}
			return !ARE_QUERY_SERVICES.test(javaClass);
		}
	};

	private static final String APPLICATION_PORT_PACKAGE = "application.port";

	private ServiceAuthArchitectureConditions() {
	}

	/**
	 * 禁止实现 {@code application.port} 下以 Port 结尾的接口。
	 */
	static ArchCondition<JavaClass> notImplementApplicationPort() {
		return new ArchCondition<>("not implement any application.port.*Port interface") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				javaClass.getInterfaces().forEach(type -> {
					JavaClass iface = type.toErasure();
					if (!isApplicationPort(iface)) {
						return;
					}
					String message = String.format("Class %s implements application Port %s", javaClass.getName(),
							iface.getName());
					events.add(SimpleConditionEvent.violated(javaClass, message));
				});
			}
		};
	}

	/**
	 * {@code application.port} 下 Port 接口的实现类必须位于 infrastructure 层。
	 */
	static ArchCondition<JavaClass> implementApplicationPortsOnlyInInfrastructure() {
		return new ArchCondition<>("implement application.port.*Port only in infrastructure") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				if (javaClass.isInterface()) {
					return;
				}
				boolean implementsApplicationPort = javaClass.getInterfaces()
					.stream()
					.map(JavaType::toErasure)
					.anyMatch(ServiceAuthArchitectureConditions::isApplicationPort);
				if (!implementsApplicationPort) {
					return;
				}
				if (javaClass.getPackageName().contains(".infrastructure.")) {
					return;
				}
				String message = String.format("Port implementation %s must reside in infrastructure layer",
						javaClass.getName());
				events.add(SimpleConditionEvent.violated(javaClass, message));
			}
		};
	}

	/**
	 * 以 Adapter 结尾且实现 application.port 接口的类，应位于 infrastructure 的 adapter 子包。
	 */
	static ArchCondition<JavaClass> resideInInfrastructureAdapterPackageWhenPortAdapter() {
		return new ArchCondition<>("reside in infrastructure adapter package when named *Adapter and implements Port") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				if (!javaClass.getSimpleName().endsWith("Adapter")) {
					return;
				}
				boolean implementsPort = javaClass.getInterfaces()
					.stream()
					.map(JavaType::toErasure)
					.anyMatch(iface -> iface.getPackageName().contains(APPLICATION_PORT_PACKAGE));
				if (!implementsPort) {
					return;
				}
				String packageName = javaClass.getPackageName();
				if (packageName.contains(".infrastructure.") && packageName.contains(".adapter")) {
					return;
				}
				String message = String.format("Port adapter %s must reside under infrastructure..adapter.. (got %s)",
						javaClass.getName(), packageName);
				events.add(SimpleConditionEvent.violated(javaClass, message));
			}
		};
	}

	private static boolean isApplicationPort(JavaClass iface) {
		return iface.isInterface() && iface.getSimpleName().endsWith("Port")
				&& iface.getPackageName().contains(APPLICATION_PORT_PACKAGE);
	}

}
