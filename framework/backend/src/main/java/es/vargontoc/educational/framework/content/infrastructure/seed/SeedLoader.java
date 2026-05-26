package es.vargontoc.educational.framework.content.infrastructure.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedLoader.class);

    private final SeedService seedService;

    public SeedLoader(SeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(String... args) {
        log.info("SeedLoader starting...");
        try {
            seedService.loadAll();
        } catch (Exception e) {
            log.error("Seed loading failed", e);
        }
    }
}
