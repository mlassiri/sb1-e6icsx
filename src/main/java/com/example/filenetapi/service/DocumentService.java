package com.example.filenetapi.service;

import com.example.filenetapi.dto.DocumentPropertiesDTO;
import com.example.filenetapi.dto.SearchRequestDTO;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private ObjectStore objectStore;

    public String createDocument(MultipartFile file, String documentClass) throws Exception {
        Document doc = Factory.Document.createInstance(objectStore, documentClass);
        
        ContentElementList contentList = Factory.ContentElement.createList();
        try (InputStream is = file.getInputStream()) {
            contentList.add(Factory.ContentTransfer.createInstance());
            doc.set_ContentElements(contentList);
            doc.set_MimeType(file.getContentType());
            doc.checkin(CheckinType.MAJOR_VERSION, "Initial Version");
            doc.save(RefreshMode.REFRESH);
            
            return doc.get_Id().toString();
        }
    }

    public Document getDocument(String id) throws Exception {
        return Factory.Document.fetchInstance(objectStore, id, null);
    }

    public void deleteDocument(String id) throws Exception {
        Document doc = Factory.Document.fetchInstance(objectStore, id, null);
        doc.delete();
        doc.save(RefreshMode.REFRESH);
    }

    public List<DocumentPropertiesDTO> searchDocuments(String sqlQuery, int maxResults) throws Exception {
        SearchScope searchScope = new SearchScope(objectStore);
        SearchSQL searchSQL = new SearchSQL(sqlQuery);
        DocumentSet documents = (DocumentSet) searchScope.fetchObjects(searchSQL, maxResults, null, true);

        List<DocumentPropertiesDTO> results = new ArrayList<>();
        for (Object obj : documents) {
            Document doc = (Document) obj;
            results.add(mapDocumentToDTO(doc));
        }

        return results;
    }

    public List<DocumentPropertiesDTO> searchDocumentsAdvanced(SearchRequestDTO searchRequest) throws Exception {
        StringBuilder sqlBuilder = new StringBuilder(searchRequest.getSqlQuery());

        // Add ORDER BY clause if specified
        if (searchRequest.getOrderBy() != null && !searchRequest.getOrderBy().isEmpty()) {
            sqlBuilder.append(" ORDER BY ")
                     .append(searchRequest.getOrderBy())
                     .append(searchRequest.isAscending() ? " ASC" : " DESC");
        }

        SearchScope searchScope = new SearchScope(objectStore);
        SearchSQL searchSQL = new SearchSQL(sqlBuilder.toString());

        // Apply parameters if provided
        if (searchRequest.getParameters() != null) {
            for (Map.Entry<String, Object> param : searchRequest.getParameters().entrySet()) {
                searchSQL.setParameter(param.getKey(), param.getValue());
            }
        }

        // Calculate page size and offset
        int pageSize = Math.min(searchRequest.getMaxResults(), 1000); // Limit max results
        int offset = Math.max(0, searchRequest.getOffset());

        DocumentSet documents = (DocumentSet) searchScope.fetchObjects(searchSQL, pageSize, null, true);

        // Skip to offset
        Iterator<?> iterator = documents.iterator();
        for (int i = 0; i < offset && iterator.hasNext(); i++) {
            iterator.next();
        }

        List<DocumentPropertiesDTO> results = new ArrayList<>();
        while (iterator.hasNext() && results.size() < pageSize) {
            Document doc = (Document) iterator.next();
            DocumentPropertiesDTO dto = mapDocumentToDTO(doc);
            
            // Filter properties if specified
            if (searchRequest.getPropertiesToInclude() != null && searchRequest.getPropertiesToInclude().length > 0) {
                Map<String, Object> filteredProps = new HashMap<>();
                Set<String> includedProps = new HashSet<>(Arrays.asList(searchRequest.getPropertiesToInclude()));
                dto.getCustomProperties().forEach((key, value) -> {
                    if (includedProps.contains(key)) {
                        filteredProps.put(key, value);
                    }
                });
                dto.setCustomProperties(filteredProps);
            }
            
            results.add(dto);
        }

        return results;
    }

    private DocumentPropertiesDTO mapDocumentToDTO(Document document) {
        Properties props = document.getProperties();
        Map<String, Object> customProperties = new HashMap<>();
        
        // Get all properties and filter out system properties
        for (Object propObj : props) {
            com.filenet.api.property.Property prop = (com.filenet.api.property.Property) propObj;
            if (!isSystemProperty(prop.getPropertyName())) {
                customProperties.put(prop.getPropertyName(), prop.getObjectValue());
            }
        }

        return DocumentPropertiesDTO.builder()
                .id(document.get_Id().toString())
                .name(document.get_Name())
                .documentClass(document.get_ClassDescription().get_SymbolicName())
                .mimeType(document.get_MimeType())
                .dateCreated(document.get_DateCreated())
                .dateLastModified(document.get_DateLastModified())
                .creator(document.get_Creator())
                .lastModifier(document.get_LastModifier())
                .customProperties(customProperties)
                .build();
    }

    private boolean isSystemProperty(String propertyName) {
        return propertyName.startsWith("F_") || 
               propertyName.equals(PropertyNames.ID) ||
               propertyName.equals(PropertyNames.NAME) ||
               propertyName.equals(PropertyNames.MIME_TYPE) ||
               propertyName.equals(PropertyNames.DATE_CREATED) ||
               propertyName.equals(PropertyNames.DATE_LAST_MODIFIED) ||
               propertyName.equals(PropertyNames.CREATOR) ||
               propertyName.equals(PropertyNames.LAST_MODIFIER);
    }
}