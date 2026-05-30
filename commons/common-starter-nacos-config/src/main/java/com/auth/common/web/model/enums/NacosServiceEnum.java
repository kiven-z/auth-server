package com.auth.common.web.model.enums;

import lombok.Getter;

/**
 * Nacos service enums.
 *
 * @author Bunny
 */
@Getter
public enum NacosServiceEnum {

	/**
	 * Auth service.
	 */
	AUTH_SERVICE_USER_CONFIG("common-config.yaml.yaml", "AUTH_COMMON"),;

	private final String filename;

	private final String group;

	NacosServiceEnum(String filename, String group) {
		this.filename = filename;
		this.group = group;
	}

}
