package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.service.auth.authorization.domain.enums.AuthorizationChangeKind;
import com.auth.service.auth.authorization.domain.invalidation.model.ImpactTarget;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 影响面解析器注册表：按 {@link AuthorizationChangeKind} 分发到对应 {@link ImpactResolver}。
 *
 * @author Bunny
 */
@Component
public class ImpactResolverRegistry {

	private final Map<AuthorizationChangeKind, ImpactResolver<?>> resolversByKind;

	public ImpactResolverRegistry(List<ImpactResolver<?>> resolvers) {
		this.resolversByKind = resolvers.stream()
			.collect(Collectors.toUnmodifiableMap(ImpactResolver::kind, Function.identity(), (left, right) -> {
				throw new IllegalStateException("Duplicate ImpactResolver for kind " + left.kind() + ": "
						+ left.getClass().getName() + " vs " + right.getClass().getName());
			}));
	}

	/**
	 * 解析失效目标为受影响用户 ID。
	 * @param target 类型化业务键
	 * @return 去重用户 ID
	 */
	public Set<Long> resolve(ImpactTarget target) {
		ImpactResolver<?> resolver = resolversByKind.get(target.kind());
		if (resolver == null) {
			throw new IllegalArgumentException("No ImpactResolver registered for kind: " + target.kind());
		}
		if (!resolver.targetType().isInstance(target)) {
			throw new IllegalArgumentException("ImpactTarget type " + target.getClass().getName()
					+ " does not match resolver " + resolver.targetType().getName() + " for kind " + target.kind());
		}
		return resolveUnchecked(resolver, target);
	}

	@SuppressWarnings("unchecked")
	private static <T extends ImpactTarget> Set<Long> resolveUnchecked(ImpactResolver<T> resolver,
			ImpactTarget target) {
		return resolver.resolve((T) target);
	}

}
