package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.services.MailProcessingService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("mail")
public class MailResource {

    @Inject
    MailProcessingService mailProcessingService;

    @POST
    @Path("sendByTemplateWithAttachments")
    @Transactional
    @Authenticated
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMailByTemplateWithAttachments(MultipartFormDataInput input) {
        try {
            mailProcessingService.sendMailByTemplateWithAttachments(input);
            return Response.ok("Emails mit Anhängen erfolgreich gesendet").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Senden der E-Mails: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("attachments/{id}")
    @Authenticated
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAttachment(@PathParam("id") Long id) {
        return mailProcessingService.getAttachment(id);
    }
}

