package com.auth.service.system.schedule.task.custom;

import com.auth.service.system.schedule.annotation.QuartzTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 演示用可调度 Bean；invoke_target 示例：{@code demoQuartzTask.run()} 或
 * {@code demoQuartzTask.echo('hi')}
 *
 * @author Bunny
 */
@QuartzTask(name = "DemoQuartzTask", description = "示例：演示任务，无业务副作用")
@Slf4j
@Component("demoQuartzTask")
public class DemoQuartzTask {

	/**
	 * 无参执行
	 * @return 固定文案
	 */
	public String run() {
		log.info("执行 run 方法，DemoQuartzTask - 示例：演示任务，无业务副作用");
		return "demo-ok";
	}

	/**
	 * 单字符串参数
	 * @param msg 参数
	 * @return 回显
	 */
	public String echo(String msg) {
		log.info("执行 echo 方法，DemoQuartzTask - 示例：演示任务，无业务副作用");
		return "echo:" + msg;
	}

}
