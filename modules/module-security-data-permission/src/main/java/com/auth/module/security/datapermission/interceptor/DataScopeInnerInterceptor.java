package com.auth.module.security.datapermission.interceptor;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.module.security.datapermission.context.LoginUserProvider;
import com.auth.module.security.datapermission.handler.DataScopeHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus 拦截器，追加数据范围条件
 *
 * <p>
 * MVP 实现使用基于字符串的 SQL 修补；复杂 SQL 不应使用 {@code @DataScope}.
 * </p>
 *
 * @author Bunny
 */
@Slf4j
public class DataScopeInnerInterceptor implements InnerInterceptor {

	/**
	 * 登录用户提供者
	 */
	private final LoginUserProvider loginUserProvider;

	/**
	 * 数据范围缓存
	 */
	private final Map<String, DataScope> dataScopeCache = new ConcurrentHashMap<>();

	public DataScopeInnerInterceptor(LoginUserProvider loginUserProvider) {
		this.loginUserProvider = loginUserProvider;
	}

	/**
	 * 追加 WHERE 条件
	 * @param sql SQL
	 * @param condition 条件
	 * @return 追加 WHERE 条件后的 SQL
	 */
	private static String appendWhereCondition(String sql, String condition) {
		if (sql == null || sql.isBlank()) {
			return sql;
		}
		String normalized = sql.toLowerCase();
		int whereIdx = normalized.indexOf(" where ");
		if (whereIdx >= 0) {
			return sql + " AND (" + condition + ")";
		}
		return sql + " WHERE (" + condition + ")";
	}

	private static void trySetSql(BoundSql boundSql, String sql) {
		try {
			Field f = boundSql.getClass().getDeclaredField("sql");
			f.setAccessible(true);
			f.set(boundSql, sql);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to patch BoundSql.sql", ex);
		}
	}

	/**
	 * 在查询之前执行
	 * @param executor 执行器
	 * @param ms 映射语句
	 * @param parameter 参数
	 * @param rowBounds 行边界
	 * @param resultHandler 结果处理器
	 * @param boundSql 绑定 SQL
	 */
	@Override
	public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
			ResultHandler resultHandler, BoundSql boundSql) {

		// 解析数据范围
		DataScope dataScope = resolveDataScope(ms);
		if (dataScope == null) {
			return;
		}

		// 获取当前登录用户
		AuthProfile profile = loginUserProvider != null ? loginUserProvider.currentUser() : null;
		if (profile == null) {
			return;
		}

		// 构建数据范围条件
		String condition = buildCondition(profile, dataScope);
		if (CharSequenceUtil.isBlank(condition)) {
			return;
		}

		// 追加 WHERE 条件
		String originalSql = boundSql.getSql();
		String patched = appendWhereCondition(originalSql, condition);
		if (!patched.equals(originalSql)) {
			trySetSql(boundSql, patched);
		}
	}

	/**
	 * 构建数据范围条件
	 * @param profile 当前用户快照
	 * @param dataScope 注解元数据
	 * @return 数据范围条件
	 */
	private String buildCondition(AuthProfile profile, DataScope dataScope) {
		try {
			DataScopeHandler handler = dataScope.handler().getDeclaredConstructor().newInstance();
			return handler.buildCondition(profile, dataScope);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to build data scope condition.", ex);
		}
	}

	/**
	 * 解析数据范围
	 * @param ms 映射语句
	 * @return 数据范围
	 */
	private DataScope resolveDataScope(MappedStatement ms) {
		if (ms == null || ms.getId() == null) {
			return null;
		}
		return dataScopeCache.computeIfAbsent(ms.getId(), this::findDataScopeByStatementId);
	}

	/**
	 * 根据语句 ID 查找数据范围
	 * @param statementId 语句 ID
	 * @return 数据范围
	 */
	private DataScope findDataScopeByStatementId(String statementId) {
		try {
			int idx = statementId.lastIndexOf('.');
			if (idx <= 0 || idx >= statementId.length() - 1) {
				return null;
			}
			String className = statementId.substring(0, idx);
			String methodName = statementId.substring(idx + 1);
			Class<?> mapperClass = Class.forName(className);
			for (Method m : mapperClass.getMethods()) {
				if (!m.getName().equals(methodName)) {
					continue;
				}
				DataScope ann = m.getAnnotation(DataScope.class);
				if (ann != null) {
					return ann;
				}
			}
			return null;
		}
		catch (Exception ex) {
			log.debug("Cannot resolve @DataScope for statementId={}", statementId, ex);
			return null;
		}
	}

}
