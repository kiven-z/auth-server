package com.auth.service.system.schedule.validation.form;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.UtilityClass;

/**
 * 表单一致性校验辅助
 *
 * @author Bunny
 */
@UtilityClass
public class SysJobFormConstraintSupport {

	/**
	 * 解析任务类型（重构后要求显式传入 taskType）
	 * @param taskTypeRaw 表单 taskType
	 * @return 任务类型
	 */
	public static SysJobTaskType resolveTaskType(String taskTypeRaw) {
		return SysJobTaskType.resolve(taskTypeRaw);
	}

	/**
	 * 新增一个字段错误
	 * @param context 校验上下文
	 * @param property 字段名
	 * @param message 消息
	 */
	public static void addViolation(ConstraintValidatorContext context, String property, String message) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addPropertyNode(property).addConstraintViolation();
	}

	/**
	 * 判断字符串是否为空白
	 * @param value 值
	 * @return 是否为空白
	 */
	public static boolean isBlank(String value) {
		return CharSequenceUtil.isBlank(value);
	}

}
