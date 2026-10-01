package es.vargontoc.educational.framework.shared.infrastructure;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public class HibernateSqlStatementCounter implements SqlStatementCounter {

    private final Statistics statistics;

    public HibernateSqlStatementCounter(EntityManagerFactory entityManagerFactory) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.statistics = sessionFactory.getStatistics();
        this.statistics.setStatisticsEnabled(true);
    }

    @Override
    public long snapshot() {
        return statistics.getPrepareStatementCount();
    }

    @Override
    public long countSince(long snapshot) {
        return statistics.getPrepareStatementCount() - snapshot;
    }
}
