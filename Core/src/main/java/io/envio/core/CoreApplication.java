package io.envio.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
	"io.envio.core.common",
	"io.envio.core.domain.project",
	"io.envio.core.domain.user"
})
@EnableJpaRepositories(basePackages = {
	"io.envio.core.domain.project.repository",
	"io.envio.core.domain.user.repository"
})
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

}
