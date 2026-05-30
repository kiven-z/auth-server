package com.auth.common.web.autoconfigure;

import com.auth.common.web.config.Knife4jConfig;
import com.auth.common.web.config.properties.WebDocProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * OpenAPI/Knife4j auto configuration.
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(WebDocProperties.class)
@Import({ Knife4jConfig.class })
public class CommonWebDocAutoConfiguration {

}
