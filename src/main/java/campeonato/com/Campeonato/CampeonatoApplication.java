package campeonato.com.Campeonato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CampeonatoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampeonatoApplication.class, args);
	}

}
