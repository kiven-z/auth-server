package com.auth.service.system.message.channel.email;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 内存中的 MultipartFile 实现，用于邮件附件组装
 *
 * @author Bunny
 */
public class InMemoryMultipartFile implements MultipartFile {

	private final String name;

	private final String originalFilename;

	private final String contentType;

	private final byte[] content;

	/**
	 * @param name 表单字段名
	 * @param originalFilename 原始文件名
	 * @param contentType MIME 类型
	 * @param content 文件内容
	 */
	public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
		this.name = name;
		this.originalFilename = originalFilename;
		this.contentType = contentType;
		this.content = content != null ? content : new byte[0];
	}

	@Override
	@NonNull
	public String getName() {
		return name;
	}

	@Override
	@Nullable
	public String getOriginalFilename() {
		return originalFilename;
	}

	@Override
	@Nullable
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return content.length == 0;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	@NonNull
	public byte[] getBytes() {
		return content;
	}

	@Override
	@NonNull
	public InputStream getInputStream() {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(@NonNull File dest) throws IOException {
		FileCopyUtils.copy(content, dest);
	}

}
