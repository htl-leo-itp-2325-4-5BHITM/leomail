package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import at.htlleonding.leomail.model.dto.contacts.*;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.model.exceptions.contacts.ContactExistsInKeycloakException;
import at.htlleonding.leomail.model.exceptions.contacts.ContactInUseException;
import at.htlleonding.leomail.model.exceptions.contacts.ContactKcUserException;
import at.htlleonding.leomail.repositories.ContactRepository;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/contacts")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    private static final Logger LOGGER = Logger.getLogger(ContactResource.class);

    @Inject
    ContactRepository contactRepository;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/add/natural")
    @Transactional
    public Response addNaturalContact(NaturalContactAddDTO contactDTO) {
        try {
            contactRepository.addContact(contactDTO);
            return Response.status(Response.Status.CREATED).entity("Natural contact created successfully.").build();
        } catch (ContactExistsInKeycloakException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (ObjectContainsNullAttributesException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getFields() + ": These fields are missing or null.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while adding natural contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @POST
    @Path("/add/company")
    @Transactional
    public Response addCompanyContact(CompanyContactAddDTO contactDTO) {
        try {
            contactRepository.addContact(contactDTO);
            return Response.status(Response.Status.CREATED).entity("Company contact created successfully.").build();
        } catch (ContactExistsInKeycloakException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (ObjectContainsNullAttributesException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getFields() + ": These fields are missing or null.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while adding company contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @GET
    @Path("/single")
    public Response getContact(@QueryParam("id") String id) {
        try {
            Object contactDetail = contactRepository.getContact(id);
            return Response.ok(contactDetail).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while fetching contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @GET
    @Path("/search/natural")
    public Response searchNaturalContacts(@QueryParam("query") String searchTerm) {
        try {
            List<NaturalContactSearchDTO> results = contactRepository.searchNaturalContacts(searchTerm, jwt.getClaim("sub"));
            return Response.ok(results).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while searching natural contacts", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @GET
    @Path("/search/company")
    public Response searchCompanyContacts(@QueryParam("query") String searchTerm) {
        try {
            List<CompanyContactSearchDTO> results = contactRepository.searchCompanyContacts(searchTerm, jwt.getClaim("sub"));
            return Response.ok(results).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while searching company contacts", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @GET
    @Path("/search/all")
    public Response searchAllContacts(@QueryParam("query") String searchTerm) {
        try {
            List<ContactSearchResult> results = contactRepository.searchAllContacts(searchTerm, jwt.getClaim("sub"));
            return Response.ok(results).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while searching all contacts", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @POST
    @Path("/update/natural")
    @Transactional
    public Response updateNaturalContact(NaturalContactDetailDTO contactDTO) {
        try {
            contactRepository.updateContact(contactDTO);
            return Response.ok().entity("Natural contact updated successfully.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while updating natural contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @POST
    @Path("/update/company")
    @Transactional
    public Response updateCompanyContact(CompanyContactDetailDTO contactDTO) {
        try {
            contactRepository.updateContact(contactDTO);
            return Response.ok().entity("Company contact updated successfully.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while updating company contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }

    @DELETE
    @Path("/delete")
    @Transactional
    public Response deleteContact(@QueryParam("id") String id) {
        try {
            contactRepository.deleteContact(id);
            return Response.ok().entity("Contact deleted successfully.").build();
        } catch (ContactKcUserException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ContactInUseException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while deleting contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred.").build();
        }
    }
}