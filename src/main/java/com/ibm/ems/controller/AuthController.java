package com.ibm.ems.controller;

import com.ibm.ems.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/login")
	public String login(@RequestParam String username, @RequestParam String password) {

		// SIMPLE CHECK (demo only)
		if ("sonu".equals(username) && "sonu".equals(password)) {
			return jwtUtil.generateToken(username);
		}

		throw new RuntimeException("Invalid credentials");
	}
}