package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import at.htlleonding.leomail.model.dto.project.MailAddressDTO;
import at.htlleonding.leomail.model.dto.project.ProjectAddDTO;
import at.htlleonding.leomail.model.dto.project.ProjectDetailDTO;
import at.htlleonding.leomail.model.dto.project.ProjectOverviewDTO;
import at.htlleonding.leomail.model.dto.user.MailAddressInformationDTO;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.services.MailService;
import at.htlleonding.leomail.services.PermissionService;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectRepository {

    @Inject
    MailService mailService;

    @Inject
    PermissionService permissionService;

    @Inject
    EntityManager em;

    /**
     * Retrieves personal projects for a given contact ID.
     *
     * @param contactId The ID of the contact.
     * @return A list of ProjectOverviewDTO.
     */
    public List<ProjectOverviewDTO> getPersonalProjects(String contactId) {
        NaturalContact contact = NaturalContact.findById(contactId);
        return em.createQuery("SELECT p FROM Project p WHERE :contact member of p.members", Project.class)
                .setParameter("contact", contact)
                .getResultList().stream().map(project -> new ProjectOverviewDTO(project.id, project.name)).toList();
    }

    /**
     * Adds a new project to the database.
     *
     * @param projectAddDTO The DTO containing project details.
     * @param creatorId     The ID of the creator contact.
     */
    @Transactional
    public void addProject(ProjectAddDTO projectAddDTO, String creatorId) {
        if (projectAddDTO == null) {
            throw new ObjectContainsNullAttributesException(List.of("**ALL NULL**"));
        }

        // Validate mandatory fields
        List<String> nullFields = Utilities.listNullFields(projectAddDTO, List.of("id", "description"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        if (projectAddDTO.mailInformation() == null) {
            throw new IllegalArgumentException("Mail information must not be null.");
        }

        // Optional Mail validation
        if (!mailService.verifyOutlookCredentials(
                projectAddDTO.mailInformation().mailAddress(),
                projectAddDTO.mailInformation().password())) {
            throw new IllegalArgumentException("Outlook credentials are invalid");
        }

        // Collect member IDs
        List<String> memberIds = projectAddDTO.members().stream()
                .map(NaturalContactSearchDTO::id)
                .collect(Collectors.toList());

        // Fetch existing contacts
        List<NaturalContact> members = NaturalContact.list("id IN ?1", memberIds);

        if (members.size() != memberIds.size()) {
            throw new IllegalArgumentException("Some contacts do not exist or are invalid.");
        }

        // Fetch the creator contact
        NaturalContact creatorContact = NaturalContact.findById(creatorId);
        if (creatorContact == null) {
            throw new IllegalArgumentException("Creator contact does not exist.");
        }

        // Ensure the creator is in the members list
        if (!members.contains(creatorContact)) {
            members.add(creatorContact);
        }

        // Create and persist the new project
        Project project = new Project(
                projectAddDTO.name(),
                projectAddDTO.description(),
                creatorContact,
                projectAddDTO.mailInformation().mailAddress(),
                projectAddDTO.mailInformation().password(),
                members
        );
        project.persist();
    }

    /**
     * Retrieves the name of a project by its ID.
     *
     * @param pid The project ID.
     * @return The name of the project.
     */
    public String getProjectName(String pid) {
        Project project = Project.findById(pid);
        if (project == null) {
            throw new IllegalArgumentException("Project not found.");
        }
        return project.name;
    }

    public ProjectDetailDTO getProject(String pid) {
        Project project = Project.findById(pid);
        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + pid + " does not exist.");
        }

        // Initialize members collection
        project.members.size();

        List<NaturalContactSearchDTO> contactSearchDTOS = project.members.stream()
                .map(contact -> new NaturalContactSearchDTO(contact.id, contact.firstName, contact.lastName, contact.mailAddress))
                .collect(Collectors.toList());

        return new ProjectDetailDTO(
                project.id,
                project.name,
                project.description,
                new MailAddressDTO(project.mailAddress, null),
                contactSearchDTOS.stream().filter(c -> !Objects.equals(c.id(), project.createdBy.id)).collect(Collectors.toList())
        );
    }

    public ProjectDetailDTO updateProject(ProjectDetailDTO projectDetailDTO) {
        if (projectDetailDTO == null) {
            throw new ObjectContainsNullAttributesException(List.of("**ALL NULL**"));
        }

        // Validate mandatory fields
        List<String> nullFields = Utilities.listNullFields(projectDetailDTO, List.of("id", "description"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        if (projectDetailDTO.mailInformation() == null) {
            throw new IllegalArgumentException("Mail information must not be null.");
        }

        if (projectDetailDTO.mailInformation().password() != null || projectDetailDTO.mailInformation().mailAddress() != null) {
            if (!mailService.verifyOutlookCredentials(
                    projectDetailDTO.mailInformation().mailAddress(),
                    projectDetailDTO.mailInformation().password())) {
                throw new IllegalArgumentException("Outlook credentials are invalid");
            }
        }

        List<String> memberIds = projectDetailDTO.members().stream()
                .map(NaturalContactSearchDTO::id)
                .collect(Collectors.toList());

        // Fetch existing contacts
        List<NaturalContact> members = NaturalContact.list("id IN ?1", memberIds);

        if (members.size() != memberIds.size()) {
            throw new IllegalArgumentException("Some contacts do not exist or are invalid.");
        }

        // Fetch the project
        Project project = Project.findById(projectDetailDTO.id());
        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + projectDetailDTO.id() + " does not exist.");
        }

        // Ensure the creator is in the members list
        if (!members.contains(project.createdBy)) {
            members.add(project.createdBy);
        }

        project.name = projectDetailDTO.name();
        project.description = projectDetailDTO.description();
        project.members = members;

        if (projectDetailDTO.mailInformation().mailAddress() != null) {
            project.mailAddress = projectDetailDTO.mailInformation().mailAddress();
            project.password = projectDetailDTO.mailInformation().password();
        }

        return projectDetailDTO;
    }

    public void deleteProject(String pid) {
        Project project = Project.findById(pid);
        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + pid + " does not exist.");
        }

        project.delete();
    }

    public MailAddressInformationDTO getProjectMail(String uid, String pid) {
        Project project = Project.find("id = ?1", pid).firstResult();

        if (project == null) {
            throw new IllegalArgumentException("Project with ID " + pid + " does not exist.");
        }

        if (!permissionService.hasPermission(pid, uid)) {
            throw new RuntimeException("Permission denied");
        }

        return new MailAddressInformationDTO(project.name, project.mailAddress, project.id);
    }
}