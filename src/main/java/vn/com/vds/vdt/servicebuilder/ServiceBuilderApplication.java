package vn.com.vds.vdt.servicebuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceBuilderApplication {

	public static void main(String[] args) {
        System.setProperty("user.timezone", "Asia/Ho_Chi_Minh");
		SpringApplication.run(ServiceBuilderApplication.class, args);
	}

}
