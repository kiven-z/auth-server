package com.auth.service.system.schedule.service;

import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;

import java.util.List;

/**
 * 任务目录服务（给前端提供白名单类与可选方法）
 *
 * @author Bunny
 */
public interface SysJobTaskCatalogService {

	/**
	 * 列出所有白名单任务类
	 * @return 白名单类列表
	 */
	List<QuartzTaskClassVO> listQuartzClasses();

	/**
	 * 列出白名单类可执行方法（仅 0 参或单 String 参数）
	 * @param className 类全限定名
	 * @return 方法列表
	 */
	List<QuartzTaskMethodVO> listCallableMethods(String className);

}
