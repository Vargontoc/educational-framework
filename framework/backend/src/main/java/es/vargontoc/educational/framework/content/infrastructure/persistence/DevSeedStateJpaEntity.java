package es.vargontoc.educational.framework.content.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "dev_seed_state")
public class DevSeedStateJpaEntity {

    @Id
    @Column(name = "seed_key", nullable = false, length = 200)
    private String seedKey;

    @Column(name = "seed_file", nullable = false, length = 100)
    private String seedFile;

    @CreatedDate
    @Column(name = "loaded_at", nullable = false)
    private LocalDateTime loadedAt;

    public DevSeedStateJpaEntity() {}

    public DevSeedStateJpaEntity(String seedKey, String seedFile) {
        this.seedKey = seedKey;
        this.seedFile = seedFile;
        this.loadedAt = LocalDateTime.now();
    }

    public String getSeedKey() {
        return seedKey;
    }

    public void setSeedKey(String seedKey) {
        this.seedKey = seedKey;
    }

    public String getSeedFile() {
        return seedFile;
    }

    public void setSeedFile(String seedFile) {
        this.seedFile = seedFile;
    }

    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(LocalDateTime loadedAt) {
        this.loadedAt = loadedAt;
    }
}
