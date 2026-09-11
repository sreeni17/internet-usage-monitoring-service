package iums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import iums.service.IngestService;

@SpringBootApplication
public class IumsApplication {

    public static void main(String[] args) {
        boolean ingest = args.length > 0 && "ingest".equals(args[0]);
        SpringApplicationBuilder builder = new SpringApplicationBuilder(IumsApplication.class);
        if (ingest) {
            builder.web(WebApplicationType.NONE);
        }
        ConfigurableApplicationContext context = builder.run(args);
        if (ingest) {
            String file = args.length > 1 ? args[1] : context.getEnvironment().getProperty("iums.dataset-path");
            context.getBean(IngestService.class).ingest(file);
            SpringApplication.exit(context, () -> 0);
        }
    }
}
