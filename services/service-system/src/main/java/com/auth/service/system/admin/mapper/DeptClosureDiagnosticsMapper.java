package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.po.dept.DeptClosureDepthChainRowPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureHealthStatsPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureParentLinkRowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门闭包表只读诊断查询
 *
 * @author bunny
 */
@Mapper
public interface DeptClosureDiagnosticsMapper {

	/**
	 * 统计父子直连缺失的活跃部门数
	 * @return 异常总数
	 */
	long countParentLinkAnomalies();

	/**
	 * 查询父子直连缺失样本
	 * @param limit 样本上限
	 * @return 异常行
	 */
	List<DeptClosureParentLinkRowPO> selectParentLinkAnomalies(@Param("limit") int limit);

	/**
	 * 统计深度链条异常的活跃部门数
	 * @return 异常总数
	 */
	long countDepthChainAnomalies();

	/**
	 * 查询深度链条异常样本
	 * @param limit 样本上限
	 * @return 异常行
	 */
	List<DeptClosureDepthChainRowPO> selectDepthChainAnomalies(@Param("limit") int limit);

	/**
	 * 活跃部门的闭包行数健康度统计
	 * @return 统计快照
	 */
	DeptClosureHealthStatsPO selectHealthStats();

}
