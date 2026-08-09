package es.vargontoc.educational.framework.agents.ports.out;

public interface OllamaManagementPort {
    
    boolean isRunning(String model);

    boolean isPulled(String model);

    void run(String model, boolean stream, Integer keepAlive);

    void stop(String model);
}
