/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WelcomeController {

	@Value("${spring.profiles.active}")
	private String profile;

	@Value("${deployed.NODE_NAME}")
	private String nodeName;

	@Value("${deployed.NODE_IP}")
	private String nodeIp;

	@Value("${deployed.NAMESPACE}")
	private String namespace;

	@Value("${deployed.POD_NAME}")
	private String podName;

	@Value("${deployed.POD_IP}")
	private String podIp;

	@Value("${deployed.JAVA_OPTS}")
	private String javaOpts;

	@GetMapping("/")
	public String welcome(Model model) {
		model.addAttribute("profile", profile);
		model.addAttribute("nodeName", nodeName);
		model.addAttribute("nodeIp", nodeIp);
		model.addAttribute("namespace", namespace);
		model.addAttribute("podName", podName);
		model.addAttribute("podIp", podIp);
		model.addAttribute("javaOpts", javaOpts);
		return "welcome";
	}

}
