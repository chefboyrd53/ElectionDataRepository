package _8.electionRepo;

import java.io.IOException;
import com.box.sdk.BoxAPIConnection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BoxSetup {

	@Value("${box.developer.token}")
	String DEV_TOKEN;
	
    @Bean
    public BoxAPIConnection boxAPIConnection() throws IOException {
        return new BoxAPIConnection(DEV_TOKEN);
    }
}