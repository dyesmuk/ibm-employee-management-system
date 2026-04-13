package com.ibm.ems.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends GenericFilter {

	private static final long serialVersionUID = 1L;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getRequestURI();

		// Skip JWT authentication for Thymeleaf web pages and public endpoints
		if (path.startsWith("/employees") || path.startsWith("/css") || path.startsWith("/js")
				|| path.startsWith("/images") || path.startsWith("/login") || path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs") || path.startsWith("/actuator")) {
			chain.doFilter(request, response);
			return;
		}

		String authHeader = httpRequest.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			try {
				String username = jwtUtil.extractUsername(token);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
						null, Collections.emptyList());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (Exception e) {
				// Token validation failed - don't set authentication
				System.out.println("Invalid JWT token: " + e.getMessage());
			}
		}

		chain.doFilter(request, response);
	}
}

//package com.ibm.ems.security;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Collections;
//
//@Component
//public class JwtFilter extends GenericFilter {
//
//	private static final long serialVersionUID = 1L;
//
//	@Autowired
//	private JwtUtil jwtUtil;
//
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//			throws IOException, ServletException {
//
//		HttpServletRequest httpRequest = (HttpServletRequest) request;
//
//		String authHeader = httpRequest.getHeader("Authorization");
//
//		if (authHeader != null && authHeader.startsWith("Bearer ")) {
//			String token = authHeader.substring(7);
//
//			String username = jwtUtil.extractUsername(token);
//
//			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
//					Collections.emptyList());
//
//			SecurityContextHolder.getContext().setAuthentication(authentication);
//		}
//
//		chain.doFilter(request, response);
//	}
//}