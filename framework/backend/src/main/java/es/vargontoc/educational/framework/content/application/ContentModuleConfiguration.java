package es.vargontoc.educational.framework.content.application;

import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.ActivityResourceRepository;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.ContentLocaleRepository;
import es.vargontoc.educational.framework.content.ports.out.DifficultyLevelRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.service.ActivityResourceService;
import es.vargontoc.educational.framework.content.service.ActivityService;
import es.vargontoc.educational.framework.content.service.CategoryService;
import es.vargontoc.educational.framework.content.service.ContentLocaleService;
import es.vargontoc.educational.framework.content.service.DifficultyLevelService;
import es.vargontoc.educational.framework.content.service.TopicService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ContentModuleConfiguration {

    @Bean
    CategoryService categoryService(CategoryRepository categoryRepository) {
        return new CategoryService(categoryRepository);
    }

    @Bean
    TopicService topicService(TopicRepository topicRepository, CategoryRepository categoryRepository) {
        return new TopicService(topicRepository, categoryRepository);
    }

    @Bean
    ActivityService activityService(ActivityRepository activityRepository, TopicRepository topicRepository) {
        return new ActivityService(activityRepository, topicRepository);
    }

    @Bean
    DifficultyLevelService difficultyLevelService(DifficultyLevelRepository difficultyLevelRepository, ActivityRepository activityRepository) {
        return new DifficultyLevelService(difficultyLevelRepository, activityRepository);
    }

    @Bean
    ActivityResourceService activityResourceService(ActivityResourceRepository activityResourceRepository, ActivityRepository activityRepository) {
        return new ActivityResourceService(activityResourceRepository, activityRepository);
    }

    @Bean
    ContentLocaleService contentLocaleService(ContentLocaleRepository contentLocaleRepository) {
        return new ContentLocaleService(contentLocaleRepository);
    }
}
