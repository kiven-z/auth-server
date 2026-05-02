// import com.auth.module.email.exception.EmailArgException;
// import com.auth.module.email.model.EmailArg;
// import com.auth.service.system.support.MailService;
// import com.icegreen.greenmail.configuration.GreenMailConfiguration;
// import com.icegreen.greenmail.junit5.GreenMailExtension;
// import com.icegreen.greenmail.util.GreenMailUtil;
// import com.icegreen.greenmail.util.ServerSetup;
// import jakarta.mail.BodyPart;
// import jakarta.mail.MessagingException;
// import jakarta.mail.Part;
// import jakarta.mail.internet.MimeMessage;
// import jakarta.mail.internet.MimeMultipart;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.extension.RegisterExtension;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.io.ByteArrayResource;
// import org.springframework.core.io.Resource;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.test.context.junit.jupiter.SpringExtension;
// import org.springframework.web.multipart.MultipartFile;
//
// import java.util.List;
// import java.util.Map;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// /**
// * GreenMail 邮件服务测试类 使用嵌入式邮件服务器进行单元测试，无需真实邮件服务器
// */
// @DisplayName("Mail1Service GreenMail 测试")
// @ExtendWith(SpringExtension.class)
// @ContextConfiguration(classes = {Mail1ServiceTestConfig.class})
// class Mail1ServiceGreenMailTest {
//
// @RegisterExtension
// static GreenMailExtension greenMail = new GreenMailExtension(
// new ServerSetup(2525, "127.0.0.1", ServerSetup.PROTOCOL_SMTP))
// .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "password"));
//
// @Autowired
// private MailService mailService;
//
// /**
// * 测试发送简单文本邮件
// */
// @Test
// @DisplayName("发送简单文本邮件")
// void testSendSimpleMail() throws MessagingException {
// // 准备测试数据
// EmailArg emailArg = createBasicEmailArg();
//
// // 执行
// mailService.sendEmail(emailArg);
//
// // 验证
// MimeMessage[] messages = greenMail.getReceivedMessages();
// assertEquals(1, messages.length, "应该收到 1 封邮件");
//
// MimeMessage message = messages[0];
// assertEquals("test@localhost", message.getFrom()[0].toString(), "发件人应该匹配");
// assertEquals("recipient@localhost",
// message.getRecipients(MimeMessage.RecipientType.TO)[0].toString(),
// "收件人应该匹配");
// assertEquals("Test Subject", message.getSubject(), "邮件主题应该匹配");
//
// String body = GreenMailUtil.getBody(message);
// assertTrue(body.contains("Test Content"), "邮件内容应该包含测试内容");
// assertFalse(body.contains("<html>"), "文本邮件不应包含HTML标签");
// }
//
// /**
// * 测试发送带附件的邮件
// */
// @Test
// @DisplayName("发送带附件的邮件")
// void testSendMailWithAttachments() throws Exception {
// // 准备附件
// MockMultipartFile file1 = new MockMultipartFile("file1", "hello.txt", "text/plain",
// "Hello World".getBytes());
// MockMultipartFile file2 = new MockMultipartFile("file2", "data.json",
// "application/json",
// "{\"key\":\"value\"}".getBytes());
// List<MultipartFile> attachments = List.of(file1, file2);
//
// EmailArg emailArg = createBasicEmailArg();
// emailArg.setAttachments(attachments);
//
// // 执行
// mailService.sendEmail(emailArg);
//
// // 验证
// MimeMessage[] messages = greenMail.getReceivedMessages();
// assertEquals(1, messages.length, "应该收到 1 封邮件");
//
// MimeMessage message = messages[0];
//
// // 验证邮件内容是混合类型
// assertInstanceOf(MimeMultipart.class, message.getContent(), "邮件内容应为复合类型");
//
// MimeMultipart multipart = (MimeMultipart) message.getContent();
// int attachmentCount = 0;
//
// for (int i = 0; i < multipart.getCount(); i++) {
// BodyPart part = multipart.getBodyPart(i);
//
// // 检查是否是附件
// if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
// attachmentCount++;
// String fileName = part.getFileName();
//
// if ("hello.txt".equals(fileName)) {
// String content = GreenMailUtil.getBody(part);
// assertTrue(content.contains("Hello World"), "hello.txt 附件内容应该匹配");
// } else if ("data.json".equals(fileName)) {
// String content = GreenMailUtil.getBody(part);
// assertTrue(content.contains("{\"key\":\"value\"}"), "data.json 附件内容应该匹配");
// }
// }
// }
//
// assertEquals(2, attachmentCount, "应该有 2 个附件");
// }
//
// /**
// * 测试发送带内联图片的邮件
// */
// @Test
// @DisplayName("发送带内联图片的邮件")
// void testSendMailWithInlineResources() throws Exception {
// // 准备内联图片资源
// Resource imageResource = new ByteArrayResource("fake-image-content".getBytes()) {
// @Override
// public String getFilename() {
// return "logo.png";
// }
// };
//
// Map<String, Resource> inlineResources = Map.of("logo", imageResource, "banner",
// new ByteArrayResource("banner-content".getBytes()));
//
// EmailArg emailArg = createBasicEmailArg();
// emailArg.setHtml(true);
// emailArg.setText("<html><body><img src='cid:logo'><img
// src='cid:banner'></body></html>");
// emailArg.setInlineResources(inlineResources);
//
// // 执行
// mailService.sendEmail(emailArg);
//
// // 验证
// MimeMessage[] messages = greenMail.getReceivedMessages();
// assertEquals(1, messages.length, "应该收到 1 封邮件");
//
// MimeMessage message = messages[0];
// assertInstanceOf(MimeMultipart.class, message.getContent(), "邮件内容应为复合类型");
//
// MimeMultipart multipart = (MimeMultipart) message.getContent();
// int inlineCount = 2;
//
// for (int i = 0; i < multipart.getCount(); i++) {
// BodyPart part = multipart.getBodyPart(i);
//
// // 检查是否是内联资源
// if (part.getDisposition() != null && part.getDisposition().equals(Part.INLINE)) {
// inlineCount++;
// String contentId = part.getHeader("Content-ID")[0];
// assertNotNull(contentId, "内联资源应有Content-ID");
// }
// }
//
// assertEquals(2, inlineCount, "应该有 2 个内联资源");
// }
//
// /**
// * 测试发送带回复地址的邮件
// */
// @Test
// @DisplayName("发送带回复地址的邮件")
// void testSendMailWithReplyTo() throws MessagingException {
// // 准备测试数据
// EmailArg emailArg = createBasicEmailArg();
// emailArg.setReplyTo("reply@localhost");
//
// // 执行
// mailService.sendEmail(emailArg);
//
// // 验证
// MimeMessage[] messages = greenMail.getReceivedMessages();
// assertEquals(1, messages.length, "应该收到 1 封邮件");
//
// MimeMessage message = messages[0];
// assertNotNull(message.getReplyTo(), "回复地址不应为空");
// assertEquals("reply@localhost", message.getReplyTo()[0].toString(), "回复地址应该匹配");
// }
//
// /**
// * 测试参数校验 - 主题为空
// */
// @Test
// @DisplayName("参数校验：主题为空时抛出异常")
// void testValidateSubject() {
// EmailArg emailArg = createBasicEmailArg();
// emailArg.setSubject(null);
//
// EmailArgException exception = assertThrows(EmailArgException.class, () ->
// mailService.sendEmail(emailArg));
// assertEquals("邮件主题不能为空", exception.getMessage());
// }
//
// /**
// * 测试参数校验 - 内容为空
// */
// @Test
// @DisplayName("参数校验：内容为空时抛出异常")
// void testValidateContent() {
// EmailArg emailArg = createBasicEmailArg();
// emailArg.setText(null);
//
// EmailArgException exception = assertThrows(EmailArgException.class, () ->
// mailService.sendEmail(emailArg));
// assertEquals("邮件内容不能为空", exception.getMessage());
// }
//
// /**
// * 测试处理空附件列表
// */
// @Test
// @DisplayName("附件列表为空时正常发送")
// void testWithEmptyAttachments() throws MessagingException {
// EmailArg emailArg = createBasicEmailArg();
// emailArg.setAttachments(List.of()); // 空附件列表
//
// mailService.sendEmail(emailArg);
//
// MimeMessage[] messages = greenMail.getReceivedMessages();
// assertEquals(1, messages.length, "应该成功发送邮件");
// }
//
// /**
// * 创建基础的邮件参数对象
// */
// private EmailArg createBasicEmailArg() {
// EmailArg emailArg = EmailArg.builder().isHtml(false).build();
// emailArg.setFrom("test@localhost");
// emailArg.setTo(new String[]{"recipient@localhost"});
// emailArg.setSubject("Test Subject");
// emailArg.setText("Test Content");
// return emailArg;
// }
//
// }