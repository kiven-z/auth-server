package com.auth.service.auth.authorization.api.dto.invalidation;

import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationOutcome;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效执行结果（对外 API）。
 *
 * @author Bunny
 */
@Schema(name = "InvalidationResponse", title = "授权失效结果")
@Getter
@Setter
public class InvalidationResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "影响面用户数")
	private int impactedUserCount;

	@Schema(title = "递增 perm_version 的用户数")
	private int versionBumpedCount;

	@Schema(title = "成功刷新画像的用户数")
	private int profileRefreshedCount;

	@Schema(title = "成功驱逐画像的用户数")
	private int profileEvictedCount;

	/**
	 * 从领域结果转换。
	 * @param outcome 领域结果
	 * @return API 响应体
	 */
	public static InvalidationResponse from(InvalidationOutcome outcome) {
		InvalidationResponse response = new InvalidationResponse();
		response.setImpactedUserCount(outcome.impactedUserCount());
		response.setVersionBumpedCount(outcome.versionBumpedCount());
		response.setProfileRefreshedCount(outcome.profileRefreshedCount());
		response.setProfileEvictedCount(outcome.profileEvictedCount());
		return response;
	}

}
