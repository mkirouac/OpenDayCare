package org.opendaycare.repositoryservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

@Configuration
@Profile("test")
public class DatabaseConfiguration {

	private static boolean initialized = false;
	
	@Bean
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
		
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		
		
		initializer.setConnectionFactory(connectionFactory);
		
		if(!initialized) {
			//TODO Workaround for the fact that the context gets reloaded and this get's re-executed for each test class. Find better way.
			initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
			initialized = true;
		}
		return initializer;
	}
	
}
