package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import at.htlleonding.leomail.model.dto.groups.GroupDetailDTO;
import at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO;
import at.htlleonding.leomail.repositories.GroupRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class GroupResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    GroupRepository groupRepository;

    @GET
    @Path("/get/personal")
    public Response getPersonalGroups(@QueryParam("pid") String projectId) {
        try {
            String userId = jwt.getSubject();
            List<GroupOverviewDTO> groups = groupRepository.getPersonalGroups(projectId, userId);
            return Response.ok(groups).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/get/details")
    public Response getGroupDetails(@QueryParam("pid") String projectId, @QueryParam("gid") String groupId) {
        try {
            String userId = jwt.getSubject();
            GroupDetailDTO groupDetails = groupRepository.getGroupDetails(projectId, userId, groupId);
            return Response.ok(groupDetails).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT).entity("E-Group-01").build();
        }
    }

    @DELETE
    @Path("/delete")
    public Response deleteGroup(@QueryParam("pid") String projectId, @QueryParam("gid") String groupId) {
        try {
            String userId = jwt.getSubject();
            groupRepository.deleteGroup(projectId, userId, groupId);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/add")
    public Response addGroup(
            @QueryParam("pid") String projectId,
            GroupDetailDTO dto) {
        try {
            String userId = jwt.getSubject();
            groupRepository.createGroup(projectId, userId, dto.description(), dto.name(), dto.members());
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("E-Group-01").build();
        }
    }

    @POST
    @Path("/update")
    public Response updateGroup(
            @QueryParam("pid") String projectId,
            GroupDetailDTO dto) {
        try {
            String userId = jwt.getSubject();
            groupRepository.updateGroup(projectId, userId, dto.description(), dto.id(), dto.name(), dto.members());
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("E-Group-01").build();
        }
    }

    @GET
    @Path("/search")
    public Response searchGroups(@QueryParam("query") String searchTerm, @QueryParam("pid") String projectId) {
        try {
            String userId = jwt.getSubject();
            List<GroupDetailDTO> results = groupRepository.searchGroups(searchTerm, projectId, userId);
            return Response.ok(results).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("E-Group-01").build();
        }
    }

    @GET
    @Path("/getUsers")
    public Response getUsers(@QueryParam("gid") String groupId, @QueryParam("pid") String projectId) {
        try {
            String userId = jwt.getSubject();
            List<ContactSearchResult> results = groupRepository.getGroupMembers(groupId, projectId, userId);
            return Response.ok(results).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("E-Group-01").build();
        }
    }
}
