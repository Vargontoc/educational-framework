package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.TracingPattern;

import java.util.List;

public interface TracingPatternUseCase {

    TracingPattern createTracingPattern(Long topicId, String name, String description, List<List<Double>> points, ContentStatus status);

    TracingPattern getTracingPattern(Long id);

    List<TracingPattern> listTracingPatterns();

    List<TracingPattern> listTracingPatternsByTopic(Long topicId);

    TracingPattern updateTracingPattern(Long id, Long topicId, String name, String description, List<List<Double>> points, ContentStatus status);
}
