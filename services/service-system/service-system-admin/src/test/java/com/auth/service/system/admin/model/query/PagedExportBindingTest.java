package com.auth.service.system.admin.model.query;

import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 分页导出双参数绑定单元测试
 *
 * @author Bunny
 */
@DisplayName("分页导出 GET 双参数绑定")
class PagedExportBindingTest {

	@Test
	@DisplayName("扁平 query 参数分别绑定到筛选对象与导出范围对象")
	void bind_whenFlatParams_bindsFilterAndScopeSeparately() {
		// 模拟 GET /export 的扁平 query 参数
		SysPermissionQuery filter = new SysPermissionQuery();
		ExportScopeQueryFields scope = new ExportScopeQueryFields();
		MutablePropertyValues values = new MutablePropertyValues().add("fromPage", 2)
			.add("toPage", 3)
			.add("pageSize", 20)
			.add("permissionCode", "sys:permission:query");

		DataBinder filterBinder = new DataBinder(filter);
		filterBinder.bind(values);
		DataBinder scopeBinder = new DataBinder(scope);
		scopeBinder.bind(values);

		assertThat(filter.getPageSize()).isEqualTo(20);
		assertThat(filter.getPermissionCode()).isEqualTo("sys:permission:query");
		assertThat(scope.getFromPage()).isEqualTo(2);
		assertThat(scope.getToPage()).isEqualTo(3);
	}

}
