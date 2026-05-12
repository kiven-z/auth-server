package com.auth.module.platform.persistence.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 平台持久化自动配置
 *
 * @author Bunny
 */
@Import({ MybatisPlusAutoConfiguration.class, })
@Configuration
public class PlatformPersistenceAutoConfiguration {

}
