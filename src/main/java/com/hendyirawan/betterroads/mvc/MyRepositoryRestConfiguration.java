package com.hendyirawan.betterroads.mvc;

import com.hendyirawan.betterroads.core.Road;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
public class MyRepositoryRestConfiguration extends
		RepositoryRestMvcConfiguration {
	
	@Override
	protected void configureRepositoryRestConfiguration(
			RepositoryRestConfiguration config) {
		super.configureRepositoryRestConfiguration(config);
		config.exposeIdsFor(Road.class);
	}

}
