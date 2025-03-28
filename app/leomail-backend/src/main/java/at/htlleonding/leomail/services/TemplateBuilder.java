package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.CompanyContact;
import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.entities.Template;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TemplateBuilder {

    private static final Logger LOGGER = Logger.getLogger(TemplateBuilder.class);

    @Inject
    Engine engine;

    /**
     * Renders templates based on the template, contacts, and personalization flag.
     *
     * @param templateId   ID of the template
     * @param contacts     List of contacts
     * @param personalized If true, the templates are personalized
     * @return List of rendered templates as strings
     */
    public List<String> renderTemplates(String templateId, List<Contact> contacts, boolean personalized) {
        List<TemplateInstance> instances = buildTemplateInstances(templateId, contacts, personalized);
        List<String> renderedTemplates = new ArrayList<>(instances.size());

        for (TemplateInstance instance : instances) {
            try {
                String rendered = instance.render();
                renderedTemplates.add(rendered);
                LOGGER.debugf("Rendered Template: %s", rendered);
            } catch (Exception e) {
                LOGGER.error("Error rendering template", e);
            }
        }

        return renderedTemplates;
    }

    /**
     * Builds a list of TemplateInstance based on the template and contacts.
     *
     * @param templateId   ID of the template
     * @param contacts     List of contacts
     * @param personalized If true, the templates are personalized
     * @return List of TemplateInstance
     */
    private List<TemplateInstance> buildTemplateInstances(String templateId, List<Contact> contacts, boolean personalized) {
        Template template = Template.findById(templateId);
        if (template == null) throw new IllegalArgumentException("Template not found.");

        String combinedTemplate = template.greeting.templateString + "<br>" + template.content;
        io.quarkus.qute.Template quteTemplate = engine.parse(combinedTemplate);
        List<String> templateVariables = extractTemplateVariables(combinedTemplate);

        List<TemplateInstance> instances = new ArrayList<>(contacts.size());

        for (Contact contact : contacts) {
            TemplateInstance instance = quteTemplate.instance();
            for (String variable : templateVariables) {
                String value = getContactValue(contact, variable);
                instance.data(variable, value != null ? value : "");
            }
            instance.data("personalized", personalized);
            instance.data("sex", contact instanceof NaturalContact naturalContact ?
                    (naturalContact.gender != null ? naturalContact.gender.toString() : "") : "");
            instances.add(instance);
        }
        return instances;
    }


    /**
     * Extracts variables from the template content.
     *
     * @param templateContent Content of the template
     * @return List of variables
     */
    private List<String> extractTemplateVariables(String templateContent) {
        List<String> variables = new ArrayList<>();
        int startIndex = 0;
        while ((startIndex = templateContent.indexOf("{", startIndex)) != -1) {
            int endIndex = templateContent.indexOf("}", startIndex);
            if (endIndex != -1) {
                String variable = templateContent.substring(startIndex + 1, endIndex).trim();
                if (!isControlStatement(variable)) {
                    variables.add(variable);
                }
                startIndex = endIndex + 1;
            } else {
                break;
            }
        }
        LOGGER.debugf("Extracted Variables: %s", variables);
        return variables;
    }

    /**
     * Checks if a variable is a control statement.
     *
     * @param variable Name of the variable
     * @return true if it is a control statement, else false
     */
    private boolean isControlStatement(String variable) {
        List<String> controlStatements = List.of("if", "else", "for", "end", "set", "define", "include", "extends");
        // Remove any leading '#' or '/'
        String strippedVariable = (variable.startsWith("#") || variable.startsWith("/")) ? variable.substring(1) : variable;
        boolean isControl = controlStatements.stream().anyMatch(strippedVariable::startsWith);
        if (isControl) {
            LOGGER.debugf("Identified Control Statement: %s", variable);
        }
        return isControl;
    }

    /**
     * Gets the value of a contact based on the variable.
     *
     * @param contact  Contact
     * @param variable Variable in the template
     * @return Value of the contact for the variable
     */
    private String getContactValue(Contact contact, String variable) {
        String value;
        if (contact instanceof NaturalContact naturalContact) {
            value = getNaturalContactValue(naturalContact, variable);
        } else if (contact instanceof CompanyContact companyContact) {
            value = getCompanyContactValue(companyContact, variable);
        } else {
            value = null;
        }
        LOGGER.debugf("Variable '%s' mapped to '%s' for Contact ID %s", variable, value, contact.id);
        return value;
    }

    /**
     * Gets the value for a NaturalContact based on the variable.
     *
     * @param contact  NaturalContact
     * @param variable Variable in the template
     * @return Value of the contact for the variable
     */
    private String getNaturalContactValue(NaturalContact contact, String variable) {
        String value;
        switch (variable.toLowerCase()) {
            case "firstname":
                value = contact.firstName;
                break;
            case "lastname":
                value = contact.lastName;
                break;
            case "mailaddress":
                value = contact.mailAddress;
                break;
            case "prefixtitle":
                value = contact.prefixTitle != null ? contact.prefixTitle : "";
                break;
            case "suffixtitle":
                value = contact.suffixTitle != null ? contact.suffixTitle : "";
                break;
            case "company":
                value = contact.company;
                break;
            case "positionatcompany":
                value = contact.positionAtCompany;
                break;
            case "gender":
            case "sex":
                value = contact.gender != null ? contact.gender.toString() : "";
                break;
            default:
                LOGGER.warnf("Unknown template variable: %s for NaturalContact", variable);
                value = null;
        }
        LOGGER.debugf("Mapping variable '%s' to value '%s' for Contact ID %s", variable, value, contact.id);
        return value;
    }

    /**
     * Gets the value for a CompanyContact based on the variable.
     *
     * @param contact  CompanyContact
     * @param variable Variable in the template
     * @return Value of the contact for the variable
     */
    private String getCompanyContactValue(CompanyContact contact, String variable) {
        String value;
        switch (variable.toLowerCase()) {
            case "companyname":
                value = contact.companyName;
                break;
            case "mailaddress":
                value = contact.mailAddress;
                break;
            default:
                LOGGER.warnf("Unknown template variable: %s for CompanyContact", variable);
                value = null;
        }
        LOGGER.debugf("Mapping variable '%s' to value '%s' for CompanyContact ID %s", variable, value, contact.id);
        return value;
    }
}