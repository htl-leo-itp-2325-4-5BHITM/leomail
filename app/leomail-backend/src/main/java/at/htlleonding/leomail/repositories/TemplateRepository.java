package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.dto.AttachmentResponseDTO;
import at.htlleonding.leomail.model.dto.TemplateDTO;
import at.htlleonding.leomail.model.dto.UsedTemplateDTO;
import at.htlleonding.leomail.model.dto.template.SentMailDTO;
import at.htlleonding.leomail.model.dto.template.TemplateAccountInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateDateInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateMetaInformationDTO;
import at.htlleonding.leomail.model.dto.template.mail.TemplateMailContactInformationDTO;
import at.htlleonding.leomail.model.enums.ContactType;
import at.htlleonding.leomail.model.exceptions.greeting.NonExistingGreetingException;
import at.htlleonding.leomail.model.exceptions.template.TemplateNameAlreadyExistsException;
import at.htlleonding.leomail.services.PermissionService;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TemplateRepository {

    private static final Logger LOG = Logger.getLogger(TemplateRepository.class);

    @Inject
    EntityManager em;

    @Inject
    PermissionService permissionService;

    /**
     * Retrieves all templates for a given project.
     */
    public List<TemplateDTO> getProjectTemplates(String projectId) {
        // No need to parse projectId as Long; treat it as String
        List<Template> templates = em.createQuery("SELECT t FROM Template t WHERE t.project.id = :pid", Template.class)
                .setParameter("pid", projectId)
                .getResultList();

        return templates.stream()
                .filter(template -> template.getClass() == Template.class)
                .map(template -> new TemplateDTO(
                        template.id, // Assuming id is String
                        template.name,
                        template.headline,
                        template.content,
                        template.filesRequired,
                        template.greeting.id,
                        template.createdBy.id,
                        template.project.id))
                .toList();
    }

    /**
     * Retrieves all template greetings.
     */
    public Set<TemplateGreeting> getAllGreetings() {
        return new HashSet<>(TemplateGreeting.listAll(Sort.descending("id")));
    }

    /**
     * Adds a new template.
     */
    @Transactional
    public TemplateDTO addTemplate(TemplateDTO templateDTO, String creator) {
        if (Template.count("name", templateDTO.name()) > 0) {
            throw new TemplateNameAlreadyExistsException();
        }

        TemplateGreeting greeting = TemplateGreeting.findById(templateDTO.greeting());
        if (greeting == null) {
            throw new NonExistingGreetingException();
        }

        Contact createdBy = Contact.findById(creator);
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator contact not found");
        }

        Project project = Project.findById(templateDTO.projectId());
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        Template template = new Template(
                templateDTO.name(),
                templateDTO.headline(),
                templateDTO.content(),
                templateDTO.filesRequired(),
                createdBy,
                greeting,
                templateDTO.projectId());
        template.persist();

        // Ensure the template has been persisted and has an ID
        em.flush(); // Ensure changes are written to the database

        return new TemplateDTO(
                template.id,
                template.name,
                template.headline,
                template.content,
                template.filesRequired,
                template.greeting.id,
                template.createdBy.id,
                template.project.id);
    }

    /**
     * Deletes a template by ID.
     */
    @Transactional
    public void deleteById(String id) {
        if (!Template.deleteById(id)) {
            throw new IllegalArgumentException("Template with id " + id + " not found");
        }
    }

    /**
     * Updates an existing template.
     */
    @Transactional
    public TemplateDTO updateTemplate(TemplateDTO templateDTO) {
        Template template = Template.findById(templateDTO.id());
        if (template == null) {
            throw new IllegalArgumentException("Template with id " + templateDTO.id() + " not found");
        }
        template.name = templateDTO.name();
        template.headline = templateDTO.headline();
        template.content = templateDTO.content();
        template.filesRequired = templateDTO.filesRequired();
        TemplateGreeting greeting = TemplateGreeting.findById(templateDTO.greeting());
        if (greeting == null) {
            throw new NonExistingGreetingException();
        }
        template.greeting = greeting;
        template.persist();

        return new TemplateDTO(
                template.id,
                template.name,
                template.headline,
                template.content,
                template.filesRequired,
                template.greeting.id,
                template.createdBy.id,
                template.project.id);
    }

    /**
     * Retrieves used templates for a project, filtered by scheduled or sent.
     */
    public List<UsedTemplateDTO> getUsedTemplates(boolean scheduled, String pid) {
        List<SentTemplate> usedTemplateList = SentTemplate.list("project.id", pid);
        List<UsedTemplateDTO> dtoList = new ArrayList<>();

        for (SentTemplate template : usedTemplateList) {
            TemplateAccountInformationDTO accountInformation = new TemplateAccountInformationDTO(
                    template.template.createdBy.id,
                    template.senderId != null ? template.senderId : null);

            TemplateDateInformationDTO dateInformation = new TemplateDateInformationDTO(
                    template.template.created,
                    template.sentOn != null ? template.sentOn : null,
                    template.scheduledAt != null ? template.scheduledAt : null);

            Hibernate.initialize(template.template.greeting);

            TemplateMetaInformationDTO metaInformation = new TemplateMetaInformationDTO(
                    template.template.name.trim(),
                    template.template.headline.trim(),
                    template.template.content.trim(),
                    template.template.greeting.id,
                    template.template.greeting.content.trim()
            );

            List<SentMailDTO> mailList = new ArrayList<>();
            for (SentMail mail : template.mails) {
                try {
                    TemplateMailContactInformationDTO contactInfo = createMailContactInfo(mail.contact);
                    mailList.add(new SentMailDTO(contactInfo, mail.actualContent.trim()));
                } catch (IllegalArgumentException e) {
                    LOG.error("Error creating contact info: " + e.getMessage(), e);
                }
            }
            SentMailDTO[] mails = mailList.toArray(new SentMailDTO[0]);

            dtoList.add(new UsedTemplateDTO(template.id.toString(), metaInformation, dateInformation, accountInformation, Arrays.stream(mails).toList(), null));
        }

        if (scheduled) {
            dtoList.removeIf(template -> template.keyDates().sentOn() != null);
        } else {
            dtoList.removeIf(template -> template.keyDates().scheduledAt() != null
                    && template.keyDates().sentOn() == null);
        }

        return dtoList;
    }

    /**
     * Retrieves a specific used template by ID.
     */
    public UsedTemplateDTO getUsedTemplate(String templateId, String projectId, String accountId) {
        SentTemplate template = SentTemplate.findById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template with id " + templateId + " not found");
        }

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User has no permission to access this template");
        }

        TemplateAccountInformationDTO accountInformation = new TemplateAccountInformationDTO(
                template.template.createdBy.id,
                template.senderId != null ? template.senderId : null);

        TemplateDateInformationDTO dateInformation = new TemplateDateInformationDTO(
                template.template.created,
                template.sentOn != null ? template.sentOn : null,
                template.scheduledAt != null ? template.scheduledAt : null);

        TemplateMetaInformationDTO metaInformation = new TemplateMetaInformationDTO(
                template.template.name.trim(),
                template.template.headline.trim(),
                template.template.content.trim(),
                template.template.greeting.id,
                template.template.greeting.content.trim());

        List<SentMailDTO> mailList = template.mails.stream()
                .map(mail -> {
                    TemplateMailContactInformationDTO contactInfo = createMailContactInfo(mail.contact);
                    return new SentMailDTO(contactInfo, mail.actualContent.trim());
                })
                .collect(Collectors.toList());

        List<AttachmentResponseDTO> attachmentDTOs = template.attachments.stream()
                .map(att -> new AttachmentResponseDTO(
                        att.id,
                        att.fileName,
                        att.contentType,
                        "/api/mail/attachments/" + att.id, // Pfad zum Download-Endpunkt
                        att.size
                ))
                .collect(Collectors.toList());


        return new UsedTemplateDTO(
                templateId,
                metaInformation,
                dateInformation,
                accountInformation,
                mailList,
                attachmentDTOs // Anhänge hinzufügen
        );
    }

    /**
     * Creates a TemplateMailContactInformationDTO from a Contact.
     */
    private TemplateMailContactInformationDTO createMailContactInfo(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }

        contact = (Contact) Hibernate.unproxy(contact);

        if (contact instanceof NaturalContact naturalContact) {
            return new TemplateMailContactInformationDTO(
                    naturalContact.id,
                    naturalContact.firstName != null ? naturalContact.firstName.trim() : "",
                    naturalContact.lastName != null ? naturalContact.lastName.trim() : "",
                    naturalContact.mailAddress != null ? naturalContact.mailAddress.trim() : "",
                    ContactType.NATURAL
            );
        } else if (contact instanceof CompanyContact companyContact) {
            return new TemplateMailContactInformationDTO(
                    companyContact.id,
                    companyContact.companyName != null ? companyContact.companyName.trim() : "",
                    null,
                    companyContact.mailAddress != null ? companyContact.mailAddress.trim() : "",
                    ContactType.COMPANY
            );
        } else {
            throw new IllegalArgumentException("Unsupported Contact type: " + contact.getClass().getName());
        }
    }

    public void deleteUsedTemplates(List<String> tids, String pid, String uid) {
        for(String tid : tids) {
            SentTemplate template = SentTemplate.findById(tid);

            if (template == null) {
                throw new IllegalArgumentException("Template with id " + tid + " not found");
            }

            if (!permissionService.hasPermission(pid, uid)) {
                throw new SecurityException("User has no permission to delete this template");
            }

            template.delete();
        }
    }

    public List<UsedTemplateDTO> searchUsedTemplates(String query, String pid, String userId) {
        if(!permissionService.hasPermission(pid, userId)) {
            throw new IllegalArgumentException("Permission denied");
        }

        List<SentTemplate> usedTemplateList = SentTemplate.list("project.id", pid);
        List<UsedTemplateDTO> dtoList = new ArrayList<>();

        for (SentTemplate template : usedTemplateList) {
            TemplateAccountInformationDTO accountInformation = new TemplateAccountInformationDTO(
                    template.template.createdBy.id,
                    template.senderId != null ? template.senderId : null);

            TemplateDateInformationDTO dateInformation = new TemplateDateInformationDTO(
                    template.template.created,
                    template.sentOn != null ? template.sentOn : null,
                    template.scheduledAt != null ? template.scheduledAt : null);

            Hibernate.initialize(template.template.greeting);

            TemplateMetaInformationDTO metaInformation = new TemplateMetaInformationDTO(
                    template.template.name.trim(),
                    template.template.headline.trim(),
                    template.template.content.trim(),
                    template.template.greeting.id,
                    template.template.greeting.content.trim()
            );

            List<SentMailDTO> mailList = new ArrayList<>();
            for (SentMail mail : template.mails) {
                try {
                    TemplateMailContactInformationDTO contactInfo = createMailContactInfo(mail.contact);
                    mailList.add(new SentMailDTO(contactInfo, mail.actualContent.trim()));
                } catch (IllegalArgumentException e) {
                    LOG.error("Error creating contact info: " + e.getMessage(), e);
                }
            }
            SentMailDTO[] mails = mailList.toArray(new SentMailDTO[0]);

            dtoList.add(new UsedTemplateDTO(template.id.toString(), metaInformation, dateInformation, accountInformation, Arrays.stream(mails).toList(), null));
        }

        if (query != null && !query.isBlank()) {
            dtoList.removeIf(template -> !template.meta().templateName().contains(query));
        }

        return dtoList;
    }
}
