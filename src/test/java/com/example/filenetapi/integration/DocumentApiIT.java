package com.example.filenetapi.integration;

import com.example.filenetapi.FileNetApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    classes = FileNetApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullDocumentLifecycle() throws Exception {
        // Create document
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        String documentId = mockMvc.perform(multipart("/api/documents")
                .file(file)
                .param("documentClass", "TestClass"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Retrieve document
        mockMvc.perform(get("/api/documents/" + documentId))
                .andExpect(status().isOk());

        // Delete document
        mockMvc.perform(delete("/api/documents/" + documentId))
                .andExpect(status().isOk());

        // Verify document is deleted
        mockMvc.perform(get("/api/documents/" + documentId))
                .andExpect(status().isNotFound());
    }
}