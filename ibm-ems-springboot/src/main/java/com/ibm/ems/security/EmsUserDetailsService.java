package com.ibm.ems.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class EmsUserDetailsService {

	@Value("${app.security.admin.username}")
	private String adminUsername;

	@Value("${app.security.admin.password}")
	private String adminPassword;

	@Value("${app.security.user.username}")
	private String username;

	@Value("${app.security.user.password}")
	private String userPassword;

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder encoder) {

		// ADMIN — HR team: full access including salary fields
		UserDetails admin = User.builder().username(adminUsername).password(encoder.encode(adminPassword))
				.roles("ADMIN", "USER").build();

		// USER — Regular employee: limited access, salary masked
		UserDetails user = User.builder().username(username).password(encoder.encode(userPassword)).roles("USER")
				.build();

		return new InMemoryUserDetailsManager(admin, user);
	}
}
