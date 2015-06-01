package com.hendyirawan.smartroad.mvc;

import com.hendyirawan.smartroad.core.Road;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@Profile("daemon")
public class MyRepositoryRestConfiguration extends
		RepositoryRestMvcConfiguration {
	
	@Override
	protected void configureRepositoryRestConfiguration(
			RepositoryRestConfiguration config) {
		super.configureRepositoryRestConfiguration(config);
		config.exposeIdsFor(Road.class);
	}

}
