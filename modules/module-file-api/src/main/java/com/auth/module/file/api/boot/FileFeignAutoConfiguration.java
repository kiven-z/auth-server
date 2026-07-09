package com.auth.module.file.api.boot;

import com.auth.module.file.api.feign.SystemFileGovernanceFeignClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 当 classpath 存在 OpenFeign 时，注册 {@link SystemFileGovernanceFeignClient}
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClient")
@EnableFeignClients(clients = { SystemFileGovernanceFeignClient.class })
public class FileFeignAutoConfiguration {

}
