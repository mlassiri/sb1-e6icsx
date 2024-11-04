package com.example.filenetapi.service;

import com.example.filenetapi.dto.DocumentPropertiesDTO;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private ObjectStore objectStore;

    @Mock
    private Document document;

    @Mock
    private ContentElementList contentElementList;

    @Mock
    private DocumentSet documentSet;

    @Mock
    private SearchScope searchScope;

    @InjectMocks
    private DocumentService documentService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );
    }

    @Test
    void createDocument_Success() throws Exception {
        try (MockedStatic<Factory.Document> documentFactory = mockStatic(Factory.Document.class);
             MockedStatic<Factory.ContentElement> contentElementFactory = mockStatic(Factory.ContentElement.class)) {
            
            documentFactory.when(() -> Factory.Document.createInstance(any(), any()))
                         .thenReturn(document);
            contentElementFactory.when(Factory.ContentElement::createList)
                               .thenReturn(contentElementList);

            when(document.get_Id()).thenReturn("test-id");
            
            String result = documentService.createDocument(mockFile, "TestClass");
            
            assertEquals("test-id", result);
            verify(document).set_ContentElements(any());
            verify(document).set_MimeType(mockFile.getContentType());
            verify(document).save(any());
        }
    }

    @Test
    void getDocument_Success() throws Exception {
        try (MockedStatic<Factory.Document> documentFactory = mockStatic(Factory.Document.class)) {
            documentFactory.when(() -> Factory.Document.fetchInstance(any(), any(), any()))
                         .thenReturn(document);
            
            Document result = documentService.getDocument("test-id");
            
            assertNotNull(result);
            assertEquals(document, result);
        }
    }

    @Test
    void deleteDocument_Success() throws Exception {
        try (MockedStatic<Factory.Document> documentFactory = mockStatic(Factory.Document.class)) {
            documentFactory.when(() -> Factory.Document.fetchInstance(any(), any(), any()))
                         .thenReturn(document);
            
            assertDoesNotThrow(() -> documentService.deleteDocument("test-id"));
            
            verify(document).delete();
            verify(document).save(any());
        }
    }

    @Test
    void searchDocuments_Success() throws Exception {
        try (MockedStatic<SearchScope> searchScopeMock = mockStatic(SearchScope.class)) {
            when(document.get_Id()).thenReturn("test-id");
            when(document.get_Name()).thenReturn("test.txt");
            when(document.get_MimeType()).thenReturn("text/plain");
            when(document.get_DateCreated()).thenReturn(new Date());
            when(document.get_DateLastModified()).thenReturn(new Date());
            when(document.get_Creator()).thenReturn("admin");
            when(document.get_LastModifier()).thenReturn("admin");
            when(document.getProperties()).thenReturn(mock(com.filenet.api.property.Properties.class));
            
            when(documentSet.iterator()).thenReturn(List.of(document).iterator());
            when(searchScope.fetchObjects(any(), anyInt(), any(), anyBoolean())).thenReturn(documentSet);
            
            List<DocumentPropertiesDTO> results = documentService.searchDocuments("SELECT * FROM Document", 100);
            
            assertNotNull(results);
            assertFalse(results.isEmpty());
            assertEquals("test-id", results.get(0).getId());
        }
    }
}