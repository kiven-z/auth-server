package com.auth.service.system.message.service.impl;

import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.mapper.EmailTemplateMapper;
import com.auth.service.system.message.model.EmailTemplateEntity;
import com.auth.service.system.message.model.po.EmailTemplatePageRowPO;
import com.auth.service.system.message.model.query.EmailTemplateQuery;
import com.auth.service.system.message.model.vo.EmailTemplateDetailVO;
import com.auth.service.system.message.service.EmailSendService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link EmailTemplateServiceImpl} 单元测试（分页与详情路径上的审计展示名补全）。
 *
 * @author Bunny
 */
@DisplayName("EmailTemplateServiceImpl 邮件模板")
@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceImplTest {

    @Mock
    private EmailTemplateMapper emailTemplateMapper;

    @Mock
    private AuditUserDisplayService auditUserDisplayService;

    @Mock
    private EmailSendService emailSendService;

    private EmailTemplateServiceImpl emailTemplateService;

    @BeforeEach
    void setUp() throws Exception {
        emailTemplateService = new EmailTemplateServiceImpl(auditUserDisplayService, emailSendService);
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(emailTemplateService, emailTemplateMapper);
    }

    @Test
    @DisplayName("分页查询：Mapper 分页后委托审计用户展示名补全")
    void getEmailTemplatePageEnrichesAuditUsernames() {
        Page<EmailTemplateEntity> pageParams = new Page<>(1, 20);
        EmailTemplateQuery query = new EmailTemplateQuery();
        Page<EmailTemplatePageRowPO> poPage = new Page<>(1, 20);
        when(emailTemplateMapper.selectListByPage(pageParams, query)).thenReturn(poPage);

        emailTemplateService.getEmailTemplatePage(pageParams, query);

        verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
    }

    @Test
    @DisplayName("按 id 查询：无记录时返回 null，不调用补全")
    void getEmailTemplateByIdReturnsNullWhenMissing() {
        when(emailTemplateMapper.selectById(100L)).thenReturn(null);

        assertThat(emailTemplateService.getEmailTemplateById(100L)).isNull();
        verifyNoInteractions(auditUserDisplayService);
    }

    @Test
    @DisplayName("按 id 查询：组装详情后补全创建人/更新人用户名")
    void getEmailTemplateByIdEnrichesSingletonList() {
        EmailTemplateEntity entity = new EmailTemplateEntity();
        entity.setId(1L);
        entity.setTemplateCode("T1");
        entity.setCreateUser(7L);
        entity.setUpdateUser(8L);
        entity.setRequireFields(null);
        when(emailTemplateMapper.selectById(1L)).thenReturn(entity);

        EmailTemplateDetailVO vo = emailTemplateService.getEmailTemplateById(1L);

        assertThat(vo).isNotNull();
        assertThat(vo.getTemplateCode()).isEqualTo("T1");
        verify(auditUserDisplayService).enrichAuditUsernames(anyList());
    }

}
