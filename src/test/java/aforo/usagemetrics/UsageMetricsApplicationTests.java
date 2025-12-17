/*package aforo.usagemetrics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = TestBootApp.class,
    properties = {
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
@ImportAutoConfiguration({
    DispatcherServletAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    HttpEncodingAutoConfiguration.class,
    JacksonAutoConfiguration.class
})
class UsageMetricsApplicationTests {

    @Test
    void contextLoads() { }
}
*/
