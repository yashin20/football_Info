package project.footballinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableCaching //스프링부트 애플리케이션에서 캐시를 활성화하기 위함.
public class FootballinfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FootballinfoApplication.class, args);
	}

	//RestTemplate Bean 등록
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
