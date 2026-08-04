package es.vargontoc.educational.framework.contact.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.vargontoc.educational.framework.contact.infrastructure.dto.ContactRequest;
import es.vargontoc.educational.framework.contact.infrastructure.dto.ContactResponse;
import es.vargontoc.educational.framework.contact.ports.in.ContactUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/contact")
@Tag(name = "Contacto", description = "Endpoint público para el envio de comentarios, sugerencias y errores")
public class ContactController {
    
    private final ContactUseCase contactUseCase;
    public ContactController(ContactUseCase contactUseCase) {
        this.contactUseCase = contactUseCase;
    }

    @PostMapping
    @Operation(summary = "Enviar mensaje de contacto")
    public ResponseEntity<ApiResponse<ContactResponse>> sendMessage(@Valid @RequestBody ContactRequest request, HttpServletRequest httpRequest){
        try {
            contactUseCase.submit(request, getClientIp(httpRequest));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(ContactResponse.ok()));
        }catch(Exception e){
            if(e instanceof ValidationException){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Some was wrong"));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(("X-Forwarded-For"));
        if(ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
