package com.auth.module.security.starter.fixture;

import com.auth.module.security.autoconfig.annotation.InternalApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/probe-internal")
public class InternalApiFixtureController {

	@InternalApi
	@GetMapping("/x")
	String internal() {
		return "ok";
	}

}
