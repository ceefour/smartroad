package com.hendyirawan.smartroad.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author ceefour
 *
 */
@Configuration
@Profile("daemon")
@EnableWebSecurity
public class MyWebSecurityConfiguration extends
		WebSecurityConfigurerAdapter {
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
		// below although it no longer throws 401 Unauthorized, it gives 302 to login page,
		// the correct way is to use configure(HttpSecurity)
		// web.ignoring().antMatchers(HttpMethod.OPTIONS);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
        http
        	.csrf().disable();
//	        .authorizeRequests()
//				.antMatchers(HttpMethod.POST, "/pic/**").permitAll()
//				.antMatchers(HttpMethod.POST, "/promos/upoint/**").permitAll()
//	        	.antMatchers(HttpMethod.GET, "/**").authenticated()
//	        	.antMatchers(HttpMethod.POST, "/**").authenticated()
//	            .and()
//	        .formLogin().and()
//	        .httpBasic();
	}

}
