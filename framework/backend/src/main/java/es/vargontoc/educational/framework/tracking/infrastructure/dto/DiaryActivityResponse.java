package es.vargontoc.educational.framework.tracking.infrastructure.dto;

public class DiaryActivityResponse {

    private Long activityId;
    private String name;
    private String category;
    /** Solo si category == "RECOGNITION" (LETTER/NUMBER/SHAPE/COLOR/ANIMAL); null en el resto. */
    private String subcategory;
    private String engine;
    private String currentDifficulty;

    public DiaryActivityResponse() {
    }

    public DiaryActivityResponse(Long activityId, String name, String category, String subcategory, String engine, String currentDifficulty) {
        this.activityId = activityId;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.engine = engine;
        this.currentDifficulty = currentDifficulty;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }
}
