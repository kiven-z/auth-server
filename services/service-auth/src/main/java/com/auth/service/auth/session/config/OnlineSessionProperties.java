package com.auth.service.auth.session.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 全局在线会话可调参数：列表预清理与僵尸索引 reconcile Job
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.auth-service.online-session")
@Configuration
@Validated
public class OnlineSessionProperties {

	/**
	 * 列表查询相关参数
	 */
	@NotNull
	private Query query = new Query();

	/**
	 * 后台 reconcile Job 参数
	 */
	@NotNull
	private Reconcile reconcile = new Reconcile();

	/**
	 * 在线列表查询参数
	 */
	@Getter
	@Setter
	public static class Query {

		/**
		 * 分页查询前预清理 global ZSet 的批次大小
		 */
		@Min(1)
		@NotNull
		private Integer preCleanupBatch = 100;

	}

	/**
	 * 全局 ZSet 僵尸索引 reconcile Job 参数
	 */
	@Getter
	@Setter
	public static class Reconcile {

		/**
		 * 是否启用 reconcile Job
		 */
		@NotNull
		private Boolean enabled = true;

		/**
		 * 每批扫描 global ZSet 条数
		 */
		@Min(1)
		@NotNull
		private Integer batchSize = 200;

		/**
		 * 单次 Job 最多执行批次数，防止长时间占用 Redis
		 */
		@Min(1)
		@NotNull
		private Integer maxRounds = 50;

		/**
		 * reconcile Job 扫描间隔（毫秒）
		 */
		@Min(60_000)
		@NotNull
		private Long intervalMs = 600_000L;

	}

}
