package com.auth.common.ip.config;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IP工具配置属性 配置前缀使用 kebab-case 遵循项目约定 配置前缀：{@code auth.common.ip-tool}
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.ip-tool")
public class IpToolProperties {

	/**
	 * 默认的IPv4数据库文件路径
	 */
	private static final String DEFAULT_IPV4_DB_PATH = "classpath:ipdb/ip2region_v4.xdb";

	/**
	 * 默认的IPv6数据库文件路径
	 */
	private static final String DEFAULT_IPV6_DB_PATH = "classpath:ipdb/ip2region_v6.xdb";

	/**
	 * 是否解析外部/公共IP地址的区域 内部/私有IP地址永远不会从数据库中解析
	 */
	private boolean resolveExternalEnabled = true;

	/**
	 * 是否启用IPv6区域解析 当禁用时，IPv6数据库不会被加载，所有IPv6区域查询返回空
	 */
	private boolean resolveIpv6Enabled = false;

	/**
	 * IPv4 数据库文件路径
	 */
	private String ipv4DbPath;

	/**
	 * IPv6 数据库文件路径
	 */
	private String ipv6DbPath;

	/**
	 * 获取默认的IPv4数据库文件路径
	 * @return 默认的IPv4数据库文件路径
	 */
	public String getDefaultIpv4DbPath() {
		return CharSequenceUtil.isNotBlank(ipv4DbPath) ? ipv4DbPath : DEFAULT_IPV4_DB_PATH;
	}

	/**
	 * 获取默认的IPv6数据库文件路径
	 * @return 默认的IPv6数据库文件路径
	 */
	public String getDefaultIpv6DbPath() {
		return CharSequenceUtil.isNotBlank(ipv6DbPath) ? ipv6DbPath : DEFAULT_IPV6_DB_PATH;
	}

}
