package com.auth.service.system.file.storage.core.classifier;

/**
 * 存储异常分类器
 *
 * <p>
 * 用来吸收不同厂商 SDK 抛出异常的形态差异，让上层模板方法可以按语义（是否 404、是否可重试等）做统一处理， 而不是硬编码
 * {@code instanceof XxxException} 或消息匹配
 * </p>
 *
 * @author Bunny
 */
public interface StorageExceptionClassifier {

	/**
	 * 判定异常是否对应「bucket 或 object 不存在」语义
	 *
	 * <p>
	 * 用于删除等幂等场景：远端已不存在时视为成功，避免因存储侧脏数据阻塞 DB 侧 purge
	 * </p>
	 * @param throwable SDK 抛出的原始异常
	 * @return true 表示对象或桶已不存在
	 */
	boolean isNotFound(Throwable throwable);

}
