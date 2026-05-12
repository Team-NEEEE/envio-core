package io.envio.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
	"io.envio.core.common",
	"io.envio.core.domain.project"
})
@EnableJpaRepositories(basePackages = {
	"io.envio.core.domain.project.repository"
})
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

}
