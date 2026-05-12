package com.example.hotelproject.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class EmailRequestDto {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    private String name;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String templateName;

    private Map<String, Object> parameters;

    private String htmlContent;

    private String textContent;

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
}