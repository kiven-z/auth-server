package com.auth.service.auth.shared.ratelimit;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import static com.auth.module.security.contract.redis.SecurityRedisKey.LOGIN_CODE_LIMIT;

/**
 * 基于 Redis 的限流切面
 *
 * <p>
 * 处理逻辑：
 * <ol>
 * <li>从方法参数中解析出用户标识符（例如 {@code "#email"}），将其值作为 Redis 的 Key；</li>
 * <li>若该 Key 不存在，则默认从 0 开始，调用 {@code redisTemplate} 自增 1；</li>
 * <li>为该 Key 设置过期时间</li>
 * <li>在过期时间窗口内，若计数超过上限（{@link #MAX_LIMIT_COUNT}），则拒绝当前请求</li>
 * </ol>
 *
 * @author Bunny
 */
@Aspect
@Component
public class RateLimitAspect {

	/**
	 * 最大限制次数
	 */
	private static final int MAX_LIMIT_COUNT = 5;

	/**
	 * SPEL 表达式前缀
	 */
	private static final String SPEL_PREFIX = "#";

	/**
	 * 表达式解析器
	 */
	private final ExpressionParser parser = new SpelExpressionParser();

	private final RedisTemplate<String, Object> redisTemplate;

	public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 检查限流
	 * @param joinPoint 连接点
	 * @param rateLimit 限流注解
	 */
	@Before("@annotation(rateLimit)")
	public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
		String principal = parsePrincipal(joinPoint, rateLimit.principal());
		if (CharSequenceUtil.isBlank(principal)) {
			throw new AuthBusinessException(AuthResultCode.BAD_REQUEST_MISSING_EMAIL_EXCEPTION);
		}

		// 获取 Redis 键并开始计数
		String redisKey = LOGIN_CODE_LIMIT.key(principal);
		Long count = redisTemplate.opsForValue().increment(redisKey);
		if (count == null) {
			throw new AuthBusinessException(AuthResultCode.TOO_MANY_REQUESTS);
		}

		// 窗口从第一次计数开始
		if (Long.valueOf(1L).equals(count)) {
			Boolean expireResult = redisTemplate.expire(redisKey, LOGIN_CODE_LIMIT.getDefaultTtl());
			if (BooleanUtil.isFalse(expireResult)) {
				throw new AuthBusinessException(AuthResultCode.TOO_MANY_REQUESTS);
			}
		}
		if (count > MAX_LIMIT_COUNT) {
			throw new AuthBusinessException(AuthResultCode.TOO_MANY_REQUESTS);
		}
	}

	/**
	 * 解析 principal 表达式
	 * @param joinPoint 连接点
	 * @param principalExpression principal 表达式
	 * @return principal
	 */
	private String parsePrincipal(JoinPoint joinPoint, String principalExpression) {
		if (CharSequenceUtil.isBlank(principalExpression)) {
			return "";
		}
		if (principalExpression.startsWith(SPEL_PREFIX)) {
			return parseSpelExpression(joinPoint, principalExpression);
		}
		return principalExpression;
	}

	/**
	 * 解析 SPEL 表达式
	 * @param joinPoint 连接点
	 * @param expression 表达式
	 * @return 表达式值
	 */
	private String parseSpelExpression(JoinPoint joinPoint, String expression) {
		try {
			Object[] args = joinPoint.getArgs();
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			String[] parameterNames = signature.getParameterNames();
			StandardEvaluationContext context = new StandardEvaluationContext();
			for (int i = 0; i < parameterNames.length; i++) {
				context.setVariable(parameterNames[i], args[i]);
			}
			String value = parser.parseExpression(expression).getValue(context, String.class);
			return value != null ? value : "";
		}
		catch (Exception e) {
			return "";
		}
	}

}