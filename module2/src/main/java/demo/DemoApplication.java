package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePropertySource;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

@SpringBootApplication
public class DemoApplication {

    Logger logger = LoggerFactory.getLogger(DemoApplication.class.getCanonicalName());

    @Resource
    private ConfigurableEnvironment environment;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @PostConstruct
    public void init() {

        try {
            final Enumeration<URL> systemProperties = getClass().getClassLoader().getResources("application.properties");

            if (systemProperties != null) {
                while (systemProperties.hasMoreElements()) {
                    final URL url = systemProperties.nextElement();
                    logger.debug("Found application properties: " + url.getPath());
                    environment.getPropertySources().addFirst(new ResourcePropertySource(new UrlResource(url)));
                }
            }

            if (environment.getActiveProfiles() != null) {
                //load profile specific properties.
                for (String profile : environment.getActiveProfiles()) {

                    final Enumeration<URL> profileSystemProperties = getClass().getClassLoader().getResources("application-" + profile + ".properties");

                    if (profileSystemProperties != null) {
                        while (profileSystemProperties.hasMoreElements()) {
                            final URL url = profileSystemProperties.nextElement();
                            logger.info("Found profile application properties: " + url.getPath());
                            environment.getPropertySources().addFirst(new ResourcePropertySource(new UrlResource(url)));
                        }
                    }
                }
            }

        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Bean
    @ConditionalOnProperty(value = { "my.test.property" }, matchIfMissing = true)
    public String myCustomBean(final Environment environment) {
        logger.error("==============================================================");
        logger.error("================ERROR===========ERROR=========================");
        logger.error("===matchIfMissing=true but we still go through this method?===");

        logger.error(environment.getProperty("my.test.property"));

        logger.error("==============ERROR===========ERROR===========================");
        logger.error("==============================================================");

        throw new IllegalStateException("It must not declare this bean!");
    }
}
