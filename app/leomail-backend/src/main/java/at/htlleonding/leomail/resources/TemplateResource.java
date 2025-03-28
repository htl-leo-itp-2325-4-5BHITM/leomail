package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.entities.TemplateGreeting;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.model.dto.UsedTemplateDTO;
import at.htlleonding.leomail.model.exceptions.contacts.ContactExistsInKeycloakException;
import at.htlleonding.leomail.model.exceptions.greeting.NonExistingGreetingException;
import at.htlleonding.leomail.model.exceptions.template.TemplateNameAlreadyExistsException;
import at.htlleonding.leomail.repositories.TemplateRepository;
import at.htlleonding.leomail.services.PermissionService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("template") // Ensure this matches the frontend request URL
public class TemplateResource {

    @Inject
    TemplateRepository templateRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    PermissionService permissionService;

    /**
     * Retrieves all templates for a given project.
     */
    @GET
    @Path("/get")
    @Authenticated
    @Produces("application/json")
    public Response getProjectTemplates(@QueryParam("pid") String projectId) {
        try {
            List<TemplateDTO> templates = templateRepository.getProjectTemplates(projectId);
            return Response.ok(templates).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Retrieves all template greetings.
     */
    @GET
    @Path("/greetings")
    @Authenticated
    @Produces("application/json")
    public Response getAllGreetings() {
        Set<TemplateGreeting> greetings = templateRepository.getAllGreetings();
        return Response.ok(greetings).build();
    }

    /**
     * Retrieves a specific greeting by ID.
     */
    @GET
    @Path("/greeting")
    @Authenticated
    @Produces("application/json")
    public Response getGreetingById(@QueryParam("gid") String id) {
        UUID greetingId;
        try {
            greetingId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid greeting ID format").build();
        }

        TemplateGreeting greeting = TemplateGreeting.findById(greetingId);
        if (greeting == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(greeting).build();
    }

    /**
     * Adds a new template.
     */
    @POST
    @Path("/add")
    @Authenticated
    @Consumes("application/json")
    @Produces("application/json")
    public Response addTemplate(TemplateDTO templateDTO) {
        try {
            System.out.println(templateDTO);
            TemplateDTO createdTemplate = templateRepository.addTemplate(templateDTO, jwt.getClaim("sub"));
            return Response.status(Response.Status.CREATED).entity(createdTemplate).build();
        } catch (TemplateNameAlreadyExistsException excp) {
            return Response.status(Response.Status.CONFLICT).entity("E-Template-01").build();
        } catch (ContactExistsInKeycloakException excp) {
            return Response.status(Response.Status.CONFLICT).entity("E-Template-02").build();
        } catch (NonExistingGreetingException excp) {
            return Response.status(Response.Status.BAD_REQUEST).entity("E-Template-03").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Retrieves a template by ID.
     */
    @GET
    @Path("/getById")
    @Authenticated
    @Produces("application/json")
    public Response getById(@QueryParam("id") String id) {
        Template template = Template.findById(id);
        if (template == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String userId = jwt.getClaim("sub");
        if (permissionService.hasPermission(template.project.id, userId)) {
            TemplateDTO templateDTO = new TemplateDTO(
                    template.id,
                    template.name,
                    template.headline,
                    template.content,
                    template.filesRequired,
                    template.greeting.id,
                    template.createdBy.id,
                    template.project.id);
            return Response.ok(templateDTO).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Deletes a template by ID.
     */
    @DELETE
    @Path("/delete")
    @Authenticated
    public Response deleteById(@QueryParam("tid") String tid, @QueryParam("pid") String pid) {
        try {
            String userId = jwt.getClaim("sub");
            if (permissionService.hasPermission(pid, userId)) {
                templateRepository.deleteById(tid);
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Updates an existing template.
     */
    @POST
    @Path("/update")
    @Authenticated
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateTemplate(TemplateDTO templateDTO) {
        try {
            String userId = jwt.getClaim("sub");
            if (permissionService.hasPermission(templateDTO.projectId(), userId)) {
                TemplateDTO updatedTemplate = templateRepository.updateTemplate(templateDTO);
                return Response.ok(updatedTemplate).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (TemplateNameAlreadyExistsException excp) {
            return Response.status(Response.Status.CONFLICT).entity("E-Template-01").build();
        } catch (ContactExistsInKeycloakException excp) {
            return Response.status(Response.Status.CONFLICT).entity("E-Template-02").build();
        } catch (NonExistingGreetingException excp) {
            return Response.status(Response.Status.BAD_REQUEST).entity("E-Template-03").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Retrieves used templates, filtered by scheduled or sent.
     */
    @GET
    @Path("/getUsedTemplates")
    @Authenticated
    @Produces("application/json")
    public Response getUsedTemplates(@QueryParam("scheduled") boolean scheduled, @QueryParam("pid") String pid) {
        try {
            String userId = jwt.getClaim("sub");
            if (!permissionService.hasPermission(pid, userId)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            List<UsedTemplateDTO> usedTemplates = templateRepository.getUsedTemplates(scheduled, pid);
            return Response.ok(usedTemplates).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Retrieves a specific used template by ID.
     */
    @GET
    @Path("/getUsedTemplate")
    @Authenticated
    @Produces("application/json")
    public Response getUsedTemplate(@QueryParam("tid") String tid, @QueryParam("pid") String pid) {
        try {
            String userId = jwt.getClaim("sub");
            UsedTemplateDTO usedTemplate = templateRepository.getUsedTemplate(tid, pid, userId);
            return Response.ok(usedTemplate).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Search used templates
     * @param query search query
     * @param pid project id
     * @return list of used templates
     */
    @GET
    @Path("/searchUsedTemplates")
    @Authenticated
    @Produces("application/json")
    public Response searchUsedTemplates(@QueryParam("query") String query, @QueryParam("pid") String pid) {
        try {
            String userId = jwt.getClaim("sub");
            List<UsedTemplateDTO> usedTemplates = templateRepository.searchUsedTemplates(query, pid, userId);
            return Response.ok(usedTemplates).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/deleteUsedTemplates")
    @Authenticated
    @Transactional
    public Response deleteUsedTemplate(List<String> tids, @QueryParam("pid") String pid) {
        try {
            String userId = jwt.getClaim("sub");
            templateRepository.deleteUsedTemplates(tids, pid, userId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}