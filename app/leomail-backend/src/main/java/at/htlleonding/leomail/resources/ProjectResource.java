package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.model.dto.project.ProjectAddDTO;
import at.htlleonding.leomail.model.dto.project.ProjectDetailDTO;
import at.htlleonding.leomail.repositories.ProjectRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/project")
public class ProjectResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    ProjectRepository projectRepository;

    @GET
    @Path("get/personal")
    @Authenticated
    @Produces("application/json")
    public Response getPersonalProjects() {
        return Response.ok(projectRepository.getPersonalProjects(jwt.getClaim("sub"))).build();
    }

    @POST
    @Transactional
    @Path("add")
    @Authenticated
    public Response addProject(ProjectAddDTO project) {
        projectRepository.addProject(project, jwt.getClaim("sub"));
        return Response.ok().build();
    }

    @GET
    @Path("get/name")
    @Authenticated
    @Produces("application/json")
    public Response getProjectName(@QueryParam("pid") String pid) {
        return Response.ok(projectRepository.getProjectName(pid)).build();
    }

    @GET
    @Path("get/single")
    @Authenticated
    @Produces("application/json")
    public Response getProject(@QueryParam("pid") String pid) {
        return Response.ok(projectRepository.getProject(pid)).build();
    }

    @DELETE
    @Path("delete")
    @Authenticated
    @Transactional
    public Response deleteProject(String pid) {
        projectRepository.deleteProject(pid);
        return Response.ok().build();
    }

    @PUT
    @Path("update")
    @Authenticated
    @Transactional
    public Response updateProject(ProjectDetailDTO project) {
        projectRepository.updateProject(project);
        return Response.ok().build();
    }

    @GET
    @Path("get/mail")
    @Authenticated
    @Produces("application/json")
    public Response getProjectMail(@QueryParam("pid") String pid) {
        return Response.ok(projectRepository.getProjectMail(jwt.getSubject(), pid)).build();
    }
}
