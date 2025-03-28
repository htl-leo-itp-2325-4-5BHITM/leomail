package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.services.ImportStatusService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/status")
@ApplicationScoped
public class StatusResource {

    @Inject
    ImportStatusService importStatusService;

    /**
     * Gibt den aktuellen Importstatus zurück.
     *
     * @return JSON-Objekt mit dem Importstatus
     */
    @GET
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImportStatus() {
        boolean isImporting = importStatusService.isImporting();
        return Response.ok(Map.of("importing", isImporting)).build();
    }
}
