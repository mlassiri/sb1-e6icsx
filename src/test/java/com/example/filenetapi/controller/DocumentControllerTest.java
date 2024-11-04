package com.example.filenetapi.controller;

import com.example.filenetapi.dto.DocumentPropertiesDTO;
import com.example.filenetapi.dto.SearchRequestDTO;
import com.example.filenetapi.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filenet.api.core.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@WithMockUser
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private Document mockDocument;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );
    }

    @Test
    void createDocument_Success() throws Exception {
        when(documentService.createDocument(any(), any())).thenReturn("test-id");

        mockMvc.perform(multipart("/api/documents")
                .file(mockFile)
                .param("documentClass", "TestClass"))
                .andExpect(status().isOk())
                .andExpect(content().string("test-id"));
    }

    @Test
    void createDocument_Failure() throws Exception {
        when(documentService.createDocument(any(), any())).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(multipart("/api/documents")
                .file(mockFile)
                .param("documentClass", "TestClass"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Test error"));
    }

    @Test
    void getDocument_Success() throws Exception {
        when(documentService.getDocument("test-id")).thenReturn(mockDocument);

        mockMvc.perform(get("/api/documents/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    void getDocument_NotFound() throws Exception {
        when(documentService.getDocument("non-existent")).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/documents/non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDocument_Success() throws Exception {
        doNothing().when(documentService).deleteDocument("test-id");

        mockMvc.perform(delete("/api/documents/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDocument_Failure() throws Exception {
        doThrow(new RuntimeException("Delete error")).when(documentService).deleteDocument("test-id");

        mockMvc.perform(delete("/api/documents/test-id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Delete error"));
    }

    @Test
    void searchDocuments_Success() throws Exception {
        DocumentPropertiesDTO dto = DocumentPropertiesDTO.builder()
                .id("test-id")
                .name("test.txt")
                .documentClass("Document")
                .mimeType("text/plain")
                .dateCreated(new Date())
                .dateLastModified(new Date())
                .creator("admin")
                .lastModifier("admin")
                .customProperties(Collections.emptyMap())
                .build();

        when(documentService.searchDocuments(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/documents/search")
                .param("sql", "SELECT * FROM Document")
                .param("maxResults", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void searchDocuments_Failure() throws Exception {
        when(documentService.searchDocuments(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Invalid SQL"));

        mockMvc.perform(get("/api/documents/search")
                .param("sql", "INVALID SQL")
                .param("maxResults", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid SQL"));
    }

    @Test
    void searchDocumentsAdvanced_Success() throws Exception {
        SearchRequestDTO searchRequest = new SearchRequestDTO();
        searchRequest.setSqlQuery("SELECT * FROM Document");
        searchRequest.setMaxResults(50);
        searchRequest.setOffset(0);
        searchRequest.setParameters(Map.of("param1", "value1"));
        searchRequest.setPropertiesToInclude(new String[]{"prop1", "prop2"});
        searchRequest.setOrderBy("DateCreated");
        searchRequest.setAscending(true);

        DocumentPropertiesDTO dto = DocumentPropertiesDTO.builder()
                .id("test-id")
                .name("test.txt")
                .documentClass("Document")
                .mimeType("text/plain")
                .dateCreated(new Date())
                .dateLastModified(new Date())
                .creator("admin")
                .lastModifier("admin")
                .customProperties(Map.of("prop1", "value1"))
                .build();

        when(documentService.searchDocumentsAdvanced(any(SearchRequestDTO.class)))
                .thenReturn(Collections.singletonList(dto));

        mockMvc.perform(post("/api/documents/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void searchDocumentsAdvanced_Failure() throws Exception {
        SearchRequestDTO searchRequest = new SearchRequestDTO();
        searchRequest.setSqlQuery("INVALID SQL");

        when(documentService.searchDocumentsAdvanced(any(SearchRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid SQL"));

        mockMvc.perform(post("/api/documents/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid SQL"));
    }
}