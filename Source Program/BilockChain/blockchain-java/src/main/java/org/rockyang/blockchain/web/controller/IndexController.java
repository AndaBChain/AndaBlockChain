package org.rockyang.blockchain.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Wang HaiTian
 */
@Controller
public class IndexController {

	@GetMapping("/")
	public String hello() {
		return "Hello blockchain.";
	}
}
