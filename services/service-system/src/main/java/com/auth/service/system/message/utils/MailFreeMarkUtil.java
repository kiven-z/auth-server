package com.auth.service.system.message.utils;

import com.auth.service.system.message.exception.EmailTemplateException;
import freemarker.template.Configuration;
import freemarker.template.Template;
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
		return FreeMarkConfigurationHolder.CONFIGURATION;
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

}
