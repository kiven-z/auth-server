package com.auth.service.system.file.storage.core.capability;

/**
 * 存储平台能力标记接口
 *
 * <p>
 * 用于让厂商私有能力（例如阿里云的图片处理样式 URL、CDN 刷新、图片鉴黄等）不污染核心 SPI； 各能力接口继承本接口，Facade 通过
 * {@code capability(Class)} 按类型解析
 * </p>
 *
 * @author Bunny
 */
public interface StoragePlatformCapability {

}
