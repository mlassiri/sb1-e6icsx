package com.example.filenetapi.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.Map;

@Data
@Builder
public class DocumentPropertiesDTO {
    private String id;
    private String name;
    private String documentClass;
    private String mimeType;
    private Date dateCreated;
    private Date dateLastModified;
    private String creator;
    private String lastModifier;
    private Map<String, Object> customProperties;
}