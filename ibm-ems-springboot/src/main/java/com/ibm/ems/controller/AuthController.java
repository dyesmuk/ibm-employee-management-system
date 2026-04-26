package com.ibm.ems.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.ems.dto.auth.LoginRequest;
import com.ibm.ems.dto.auth.TokenResponse;
import com.ibm.ems.security.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login and obtain JWT token")
// See SecurityConfig for the CrossOrigin config 
//@CrossOrigin(origins = { "http://localhost:5172", "http://localhost:5173", })

public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final long expirationMs;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			@Value("${jwt.expiration.ms:3600000}") long expirationMs) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.expirationMs = expirationMs;
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate and receive a JWT token", description = "Send username and password as JSON body. Returns a Bearer token.")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {

		// Spring Security verifies credentials against UserDetailsService
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		// Extract granted roles to embed in JWT
		List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

		String token = jwtUtil.generateToken(authentication.getName(), roles);

		return ResponseEntity.ok(new TokenResponse(token, expirationMs, authentication.getName()));
	}
}
