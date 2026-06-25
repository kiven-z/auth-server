package com.auth.service.system.message.channel.email.transport;

import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 内存中的MultipartFile，用于邮件附件组装
 *
 * @author Bunny
 */
public class InMemoryMultipartFile implements MultipartFile {

	private final String name;

	private final String originalFilename;

	private final String contentType;

	private final byte[] content;

	public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
		this.name = name;
		this.originalFilename = originalFilename;
		this.contentType = contentType;
		this.content = Objects.requireNonNullElse(content, new byte[0]);
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
