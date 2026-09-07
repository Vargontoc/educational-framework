package es.vargontoc.educational.framework.audio.infrastructure.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import es.vargontoc.educational.framework.audio.domain.AudioCache;
import es.vargontoc.educational.framework.audio.infrastructure.config.AudioCacheConfiguration;
import es.vargontoc.educational.framework.shared.exception.AppException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AudioCacheStorage {
    
    private final Path cachePath;
    private final AudioCacheConfiguration properties;

    private final Cache<AudioCache, byte[]> internalCache;

    public AudioCacheStorage(
        @Value("classpath:/stories") Resource cachePath,
        AudioCacheConfiguration properties) {
        this.properties = properties;
        try {
            this.cachePath = Path.of(cachePath.getURI());
            if(!Files.exists(this.cachePath)){
                Files.createDirectories(this.cachePath);
            }
        }catch(IOException e) {
            log.error("Error en ruta cache: {}", e.getMessage(), e);
            throw new AppException("Error en obtener la ruta de cache", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.internalCache = Caffeine.newBuilder()
            .maximumSize(properties.maxEntries())
            .expireAfterWrite(Duration.ofMinutes(properties.expireAfterWriteMinutes()))
            .build();
    }

    public byte[] get(AudioCache key) {
        byte[] result = internalCache.getIfPresent(key);
        if(result != null){
            log.debug("Internal cache hit: {}", key);
            return result;
        }

        result = loadFromDisk(key);

        if(result != null){
            log.info("Disk cache hit, promoting to internal: {}", key);
            internalCache.put(key, result);
            return result;
        }

        log.warn("Cache miss: {}", key);
        return null;
    }

    public void put(AudioCache key, byte[] data){
        internalCache.put(key, data);
        saveToDisk(key, data);
        log.info("Cached {} byte: {}", data.length, key);
    }


    private void saveToDisk(AudioCache key, byte[] audio){
        Path file = resolveDiskPath(key);
        try
        {
            Files.write(file, audio);
            enforceDiskCapacity();
        } catch (IOException e){
            log.error("Error escritura en disco {}: {}", file, e.getMessage());
        }
    }

    private void enforceDiskCapacity() {
        try {
            var files = Files.list(cachePath).filter(p -> p.toString().endsWith(".mp3")).toList();
            if(files.size() <= properties.maxDiskEntries())
                return;

            int toDelete = files.size() - properties.maxDiskEntries();
            files.stream().sorted(Comparator.comparingLong(f -> {
                try {
                    return Files.getLastModifiedTime(f).toMillis();
                }catch(IOException e) { return 0L;}
            }))
            .limit(toDelete)
            .forEach(f -> {
                try{ Files.delete(f); }
                catch(IOException e){ log.warn("Failed to delete old cache file {}", f);}
            });
            log.info("Cleaned {} old files from disk", toDelete);
        }catch(IOException e) {
            log.error("Failed to enforce disk capacity: {}", e.getMessage());
        }
    }

    private byte[] loadFromDisk(AudioCache key) {
        Path file = resolveDiskPath(key);
        if(!Files.exists(file))
            return null;
        
        try {
            byte[] data = Files.readAllBytes(file);
            if(data == null || data.length == 0)
            {
                try { Files.deleteIfExists(file); } catch(IOException e) { log.info("Excepcion ignorada al borrar un fichero vacio"); }
            }
            return data;
        }catch(IOException e){
            log.error("Error al leer en disco {}: {}", file, e.getMessage());
            try { Files.deleteIfExists(file); } catch(IOException e1) { }
            return null;
        }
    }

    private Path resolveDiskPath(AudioCache key){
        String encoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(digest(key))
            .replace("/", "_")
            .replace("+", "-");
        return cachePath.resolve(encoded + ".mp3");
    }

    private byte[] digest(AudioCache key){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(key.toString().getBytes());
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            return String.valueOf(key.hashCode()).getBytes();
        }
    }

}
