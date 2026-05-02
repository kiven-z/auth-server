package com.auth.common.web.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.io.IOException;

/**
 * Controller字符串参数自动去除空格配置 该配置类用于自动去除前端传递的字符串参数中的前后空格
 *
 * @author Bunny
 */
@ConditionalOnMissingBean(ControllerStringParamTrimConfig.class)
@ControllerAdvice
public class ControllerStringParamTrimConfig {

	/**
	 * 初始化数据绑定器，注册字符串参数的空格处理编辑器 配置StringTrimmerEditor来处理字符串参数，保留空字符串而非转换为null
	 * 如果需要去除所有空格：return StringUtils.trimAllWhitespace(jsonParser.getValueAsString());
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		// 创建字符串修剪编辑器，参数false表示空格字符串不转换为null
		StringTrimmerEditor propertyEditor = new StringTrimmerEditor(false);
		// 为String类型注册自定义编辑器，处理参数绑定时的空格问题
		binder.registerCustomEditor(String.class, propertyEditor);
	}

	/**
	 * 配置Jackson对象映射器，自定义字符串类型的反序列化行为 用于处理JSON格式请求体中的字符串参数空格问题 如果要自定义，需要声明一个名为
	 * stringTrimJacksonCustomizer 的Bean，
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer stringTrimJacksonCustomizer() {
		return jacksonObjectMapperBuilder ->
		// 为String类型配置自定义反序列化器，处理JSON中的字符串参数
		jacksonObjectMapperBuilder.deserializerByType(String.class, new StdScalarDeserializer<String>(String.class) {
			@Override
			public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
				// 仅去除字符串前后的空格，保留中间的空格
				return jsonParser.getValueAsString().trim();
			}
		});
	}

}
