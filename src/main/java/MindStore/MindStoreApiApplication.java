package MindStore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MindStoreApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MindStoreApiApplication.class, args);
	}
}
