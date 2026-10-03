package es.vargontoc.educational.framework.content.infrastructure.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.vargontoc.educational.framework.agents.application.ports.in.ContentGenerationUseCase;
import es.vargontoc.educational.framework.agents.domain.request.GenerateCuriosityRequest;
import es.vargontoc.educational.framework.agents.domain.response.GenerateCuriosityResponse;
import es.vargontoc.educational.framework.shared.api.ApiResponse;



@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/content/curiosities")
public class CuriosityController {

    private final ContentGenerationUseCase generator;
    public CuriosityController(ContentGenerationUseCase generator) {

        this.generator = generator;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GenerateCuriosityResponse>> generate(@RequestBody GenerateCuriosityRequest request){
        return ResponseEntity.ok(ApiResponse.ok(generator.generateCuriosity(request)));
    }


}
