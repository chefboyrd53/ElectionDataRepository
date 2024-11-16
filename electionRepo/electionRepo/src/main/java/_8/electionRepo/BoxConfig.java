package _8.electionRepo;

import com.box.sdk.BoxAPIConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BoxConfig {

    @Bean
    public BoxAPIConnection boxAPIConnection() {
        return new BoxAPIConnection("your-developer-token");
    }
}
