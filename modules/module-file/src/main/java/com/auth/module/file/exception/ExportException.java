package com.auth.module.file.exception;

/**
 * Export validation failure with an English message for API and logs.
 *
 * @author Bunny
 */
public class ExportException extends RuntimeException {

	/**
	 * @param message English error message
	 */
	public ExportException(String message) {
		super(message);
	}

	/**
	 * @param pageSize invalid page size
	 * @return export exception
	 */
	public static ExportException pageSizeInvalid(int pageSize) {
		return new ExportException("Invalid export page size: pageSize=" + pageSize);
	}

	/**
	 * @param fromPage start page
	 * @param toPage end page
	 * @return export exception
	 */
	public static ExportException pageRangeInvalid(int fromPage, int toPage) {
		return new ExportException("Invalid export page range: fromPage=" + fromPage + ", toPage=" + toPage);
	}

	/**
	 * @param maxRows maximum allowed rows
	 * @param requestedRows requested rows
	 * @return export exception
	 */
	public static ExportException rowLimitExceeded(long maxRows, long requestedRows) {
		return new ExportException("Export row count exceeds limit: max=" + maxRows + ", requested=" + requestedRows);
	}

	/**
	 * @return export exception when fromPage/toPage are missing
	 */
	public static ExportException pageRangeRequired() {
		return new ExportException("Export page range is required: fromPage and toPage must be provided");
	}

	/**
	 * @return export exception when pageIndex is missing or invalid
	 */
	public static ExportException currentPageIndexRequired() {
		return new ExportException("Current page index is required for CURRENT_PAGE export scope");
	}

	/**
	 * @return export exception when full async export is not available yet
	 */
	public static ExportException fullAsyncNotSupported() {
		return new ExportException("Full async export is not supported yet");
	}

}
