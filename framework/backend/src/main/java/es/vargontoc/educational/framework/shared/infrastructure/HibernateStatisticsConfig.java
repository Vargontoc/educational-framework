package es.vargontoc.educational.framework.shared.infrastructure;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class HibernateStatisticsConfig {

    @Bean
    @Profile({"dev", "test"})
    public SqlStatementCounter hibernateSqlStatementCounter(EntityManagerFactory entityManagerFactory) {
        return new HibernateSqlStatementCounter(entityManagerFactory);
    }

    @Bean
    @Profile({"!dev & !test"})
    public SqlStatementCounter noopSqlStatementCounter() {
        return SqlStatementCounter.NOOP;
    }
}
