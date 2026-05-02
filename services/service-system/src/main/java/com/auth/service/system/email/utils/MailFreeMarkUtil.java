package com.auth.service.system.email.utils;

import com.auth.service.system.email.exception.EmailTemplateException;
import com.auth.service.system.email.exception.FreeMarkerConfigException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;

/**
 * freemarker 工具类
 *
 * @author Bunny
 */
@UtilityClass
@Slf4j
public class MailFreeMarkUtil {

	/**
	 * 获取Configuration实例（单例模式，双重检查锁）
	 */
	private static Configuration getConfiguration() {
		return ConfigurationHolder.CONFIGURATION;
	}

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

	/**
	 * 处理模板（不使用缓存）
	 * @param templateName 模板名称（用于标识）
	 * @param templateContent 模板内容
	 * @param dataModel 数据模型
	 * @return 处理后的字符串
	 */
	public static String processTemplate(String templateName, String templateContent, Object dataModel) {
		try {
			Configuration cfg = getConfiguration();

			// 直接从字符串创建Template对象
			Template template = new Template(templateName, templateContent, cfg);

			try (StringWriter writer = new StringWriter()) {
				template.process(dataModel, writer);
				return writer.toString();
			}
		}
		catch (Exception e) {
			log.error("模板处理失败，模板名称: {}", templateName, e);
			throw new EmailTemplateException("模板处理失败: " + e.getMessage());
		}
	}

	private static final class ConfigurationHolder {

		/**
		 * 单例Configuration实例
		 */
		private static final Configuration CONFIGURATION = createConfiguration();

	}

}
