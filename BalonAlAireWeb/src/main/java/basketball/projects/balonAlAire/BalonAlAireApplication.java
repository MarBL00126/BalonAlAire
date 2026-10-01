package basketball.projects.balonAlAire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BalonAlAireApplication {

	public static void main(String[] args) {
		SpringApplication.run(BalonAlAireApplication.class, args);
	}

}
