package com.auth.service.system.admin.convert.reference;

import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.model.po.user.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.user.UserPostProfilePO;
import com.auth.service.system.admin.model.vo.reference.ext.UserDeptReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.UserPostReferenceVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ReferenceConverter} 用户档案关联投影单元测试。
 *
 * @author Bunny
 */
@DisplayName("ReferenceConverter 用户档案关联投影")
class ReferenceConverterTest {

	@Test
	@DisplayName("部门档案：保留部门启用 status 与 relationStatus")
	void toUserDeptReferenceMapsDeptAndRelationFields() {
		UserDeptProfilePO po = new UserDeptProfilePO();
		po.setId(10L);
		po.setDeptName("研发部");
		po.setDeptCode("RD");
		po.setStatus(false);
		po.setIsPrimary(true);
		po.setRelationStatus(2);

		UserDeptReferenceVO vo = ReferenceConverter.INSTANCE.toUserDeptReference(po);

		assertThat(vo.getId()).isEqualTo(10L);
		assertThat(vo.getDeptName()).isEqualTo("研发部");
		assertThat(vo.getDeptCode()).isEqualTo("RD");
		assertThat(vo.getStatus()).isFalse();
		assertThat(vo.getIsPrimary()).isTrue();
		assertThat(vo.getRelationStatus()).isEqualTo(2);
	}

	@Test
	@DisplayName("岗位档案：保留岗位启用 status 与 relationStatus")
	void toUserPostReferenceMapsPostAndRelationFields() {
		UserPostProfilePO po = new UserPostProfilePO();
		po.setId(20L);
		po.setPostName("工程师");
		po.setPostCode("ENG");
		po.setStatus(false);
		po.setIsPrimary(true);
		po.setRelationStatus(1);

		UserPostReferenceVO vo = ReferenceConverter.INSTANCE.toUserPostReference(po);

		assertThat(vo.getId()).isEqualTo(20L);
		assertThat(vo.getPostName()).isEqualTo("工程师");
		assertThat(vo.getPostCode()).isEqualTo("ENG");
		assertThat(vo.getStatus()).isFalse();
		assertThat(vo.getIsPrimary()).isTrue();
		assertThat(vo.getRelationStatus()).isEqualTo(1);
	}

}
