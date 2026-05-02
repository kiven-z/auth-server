package com.auth.common.web.valid.list;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

/**
 * 列表值校验器 判断传过来的值是否在填写的 vals 中
 *
 * @author Bunny
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValueValid, Integer> {

	/**
	 * 设定值
	 */
	private final Set<Integer> set = new HashSet<>();

	/**
	 * 初始化 将当前值放到集合中
	 * @param constraintAnnotation 传入值是否在指定集合中
	 */
	@Override
	public void initialize(ListValueValid constraintAnnotation) {
		int[] vals = constraintAnnotation.vals();
		for (int val : vals) {
			set.add(val);
		}
	}

	/**
	 * 当前值是否在指定集合范围
	 * @param value 当前值
	 * @param context 约束验证器上下文
	 * @return 是否在指定集合中
	 */
	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		return set.contains(value);
	}

}
