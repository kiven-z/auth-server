package com.auth.service.system.message.utils;

import com.auth.service.system.message.exception.FreeMarkerConfigException;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * FreeMarker配置
 *
 * @author Bunny
 */
@Slf4j
public final class FreeMarkConfigurationHolder {

	/**
	 * 单例Configuration实例
	 */
	public static final Configuration CONFIGURATION = createConfiguration();

	/**
	 * 创建FreeMarker配置
	 */
	private static Configuration createConfiguration() {
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
			// 设置模板加载器（不设置文件加载器，只使用字符串模板）
			// 这里不需要设置DirectoryTemplateLoader

			// 设置默认编码
			cfg.setDefaultEncoding("UTF-8");
			cfg.setOutputEncoding("UTF-8");

			// 设置模板异常处理器
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

			// 设置数字格式
			cfg.setNumberFormat("#0.#####");

			// 设置布尔值格式
			cfg.setBooleanFormat("true,false");

			// 设置时间格式
			cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
			cfg.setDateFormat("yyyy-MM-dd");
			cfg.setTimeFormat("HH:mm:ss");

			// 设置空值处理，兼容空值，不抛出异常
			cfg.setClassicCompatible(true);

			// 设置缓存（根据需求选择）
			cfg.setCacheStorage(new freemarker.cache.MruCacheStorage(20, 250));

			return cfg;
		}
		catch (Exception e) {
			log.error("初始化FreeMarker配置失败", e);
			throw new FreeMarkerConfigException("初始化FreeMarker配置失败");
		}
	}

}