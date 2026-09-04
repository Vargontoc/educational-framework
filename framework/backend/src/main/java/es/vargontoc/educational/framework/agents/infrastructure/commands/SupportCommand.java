package es.vargontoc.educational.framework.agents.infrastructure.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.contact.model.ContactMessageType;
import es.vargontoc.educational.framework.contact.ports.in.ContactUseCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SupportCommand implements ChatCommand {
    Logger LOG = LoggerFactory.getLogger(SupportCommand.class);

    private final ContactUseCase contact;
    private final ChatClient supportClient;
    public SupportCommand(ContactUseCase contact, @Qualifier("support-agent") ChatClient supportAgent) {
        this.contact = contact;
        this.supportClient = supportAgent;
    }

    @Override
    public String getTrigger() {
        return "/soporte";
    }

    @Override
    public String getDescription() {
        return "Envia un mensaje al equipo de soporte. Tipos: bug, sugerencia o comentario";
    }

    @Override
    public String execute(String args) {
        String mensaje = args.trim();

        if(mensaje.isEmpty()) {
            return """
                   ❌ El mensaje de soporte no puede estar vacío. 
                   Uso: /soporte [describe aquí tu problema, duda o sugerencia]
                   Ejemplo: /soporte Hay un error visual cuando Nubi se duerme.
                   """;
        }

        try {
            contact.submit(mensaje, classyfy(mensaje), "");
            return String.format("""
                   📩 **¡Mensaje enviado con éxito!**
                   Clasificado como: *[%s]*
                   El equipo de My Friend Nubi revisará tu reporte lo antes posible. ¡Gracias por ayudarnos a mejorar!
                   """, "COMENTARIO");
        }catch(Exception e) {
            LOG.error("Algo fue mal", e);
            return "Hubo un problema enviando el mensaje a soporte. Por favor intentelo mas tarde";
        }
    }

    private ContactMessageType classyfy(String  message) {
        String result = supportClient.prompt().user(message).call().content().trim().toUpperCase();
        try {
            return ContactMessageType.valueOf(result);
        }catch(Exception e){
            return ContactMessageType.COMMENT;
        }
    }
    
    
}
