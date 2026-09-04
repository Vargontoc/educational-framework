package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.RecognitionCategory;

import java.util.List;

public interface FilterAllowedRecognitionCategoriesUseCase {

    List<RecognitionCategory> filterAllowedCategories(Long childProfileId, List<RecognitionCategory> candidateCategories);
}
