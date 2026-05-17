package com.auth.service.auth.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.core.model.response.Result;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.module.message.email.EmailSendDTO;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.mapper.UserMapper;
import com.auth.service.auth.authentication.service.impl.AuthMessageServiceImpl;
import com.auth.service.auth.support.integration.SystemMessageSendFeignClient;
import com.auth.service.auth.support.message.AuthEmailTemplateCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthMessageServiceImpl} 单元测试
 */
@DisplayName("认证消息服务 - 登录邮箱验证码")
@ExtendWith(MockitoExtension.class)
class AuthMessageServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IpAddressService ipAddressService;

    @Mock
    private SystemMessageSendFeignClient systemMessageSendFeignClient;

    private AuthMessageServiceImpl authMessageService;

    @Test
    @DisplayName("发送登录验证码：写入 Redis MD5 并调用 system 邮件接口")
    void sendEmailCode_shouldStoreMd5AndInvokeEmailSend() {
        LoginAuditSnapshotFactory snapshotFactory = new LoginAuditSnapshotFactory(ipAddressService);
        authMessageService = new AuthMessageServiceImpl(redisTemplate, userMapper, snapshotFactory,
                systemMessageSendFeignClient);

        String email = "bunny@example.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserEntity user = new UserEntity();
        user.setUsername("Bunny");

        when(userMapper.selectUserByEmail(email)).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(ipAddressService.resolveIpInfo(request))
                .thenReturn(IpInfo.builder().ipAddr("192.168.1.1").ipRegion("内网IP").build());
        when(systemMessageSendFeignClient.sendEmail(any(EmailSendDTO.class))).thenReturn(Result.success());

        authMessageService.sendEmailCode(email, request);

        ArgumentCaptor<String> redisValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(EMAIL_CODE.key(email)), redisValueCaptor.capture(),
                eq(EMAIL_CODE.getDefaultTtl()));

        ArgumentCaptor<EmailSendDTO> dtoCaptor = ArgumentCaptor.forClass(EmailSendDTO.class);
        verify(systemMessageSendFeignClient).sendEmail(dtoCaptor.capture());

        EmailSendDTO dto = dtoCaptor.getValue();
        assertEquals(AuthEmailTemplateCode.LOGIN_EMAIL_CODE.getTemplateCode(), dto.getTemplateCode());
        assertEquals(1, dto.getTo().size());
        assertEquals(email, dto.getTo().get(0));
        assertEquals(Boolean.TRUE, dto.getHasHtml());

        Object codeVar = dto.getVariables().get("verificationCode");
        assertNotNull(codeVar);
        String code = codeVar.toString();
        assertEquals(6, code.length());
        assertEquals(MD5.create().digestHex16(code), redisValueCaptor.getValue());

        assertEquals("Bunny", dto.getVariables().get("username"));
        assertEquals(EMAIL_CODE.getDefaultTtl().toMinutes(), dto.getVariables().get("expireTime"));
        assertEquals("192.168.1.1", dto.getVariables().get("ipAddr"));
        assertEquals("内网IP", dto.getVariables().get("ipRegion"));
    }

}
