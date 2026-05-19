package com.auth.service.system.message.template;

import com.auth.service.system.common.exception.TemplateRenderException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;

/**
 * FreeMarker string-template renderer (shared across message channels).
 *
 * @author Bunny
 */
@UtilityClass
@Slf4j
public class FreemarkerTemplateRenderer {

	/**
	 * Singleton {@link Configuration} instance.
	 */
	public static final Configuration CONFIGURATION = createConfiguration();

	/**
	 * Renders a template from in-memory content (no file loader).
	 * @param templateName logical template name (for error messages)
	 * @param templateContent template source
	 * @param dataModel data model
	 * @return rendered text
	 */
	public static String processTemplate(String templateName, String templateContent, Object dataModel) {
		try {
			Template template = new Template(templateName, templateContent, CONFIGURATION);
			try (StringWriter writer = new StringWriter()) {
				template.process(dataModel, writer);
				return writer.toString();
			}
		}
		catch (Exception e) {
			log.error("Template render failed, templateName={}", templateName, e);
			throw new TemplateRenderException("Template render failed: " + e.getMessage(), e);
		}
	}

	private static Configuration createConfiguration() {
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
			cfg.setDefaultEncoding("UTF-8");
			cfg.setOutputEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setNumberFormat("#0.#####");
			cfg.setBooleanFormat("true,false");
			cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
			cfg.setDateFormat("yyyy-MM-dd");
			cfg.setTimeFormat("HH:mm:ss");
			cfg.setClassicCompatible(true);
			cfg.setCacheStorage(new freemarker.cache.MruCacheStorage(20, 250));
			return cfg;
		}
		catch (Exception e) {
			log.error("Failed to initialize FreeMarker configuration", e);
			throw new TemplateRenderException("Failed to initialize FreeMarker configuration", e);
		}
	}

}
