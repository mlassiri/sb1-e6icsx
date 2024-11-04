package com.example.filenetapi.controller;

import com.example.filenetapi.dto.DocumentPropertiesDTO;
import com.example.filenetapi.dto.SearchRequestDTO;
import com.example.filenetapi.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Operations", description = "APIs for managing documents in FileNet P8")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Operation(summary = "Create a new document", description = "Upload a file to create a new document in FileNet")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDocument(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document class in FileNet") @RequestParam("documentClass") String documentClass) {
        try {
            String documentId = documentService.createDocument(file, documentClass);
            return ResponseEntity.ok().body(documentId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get document by ID", description = "Retrieve a document's metadata by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document found",
                    content = @Content(schema = @Schema(implementation = DocumentPropertiesDTO.class))),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@Parameter(description = "Document ID") @PathVariable String id) {
        try {
            return ResponseEntity.ok(documentService.getDocument(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete document", description = "Delete a document by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Error deleting document")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@Parameter(description = "Document ID") @PathVariable String id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Search documents (Simple)", description = "Search for documents using a simple SQL query")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = DocumentPropertiesDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search query")
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(
            @Parameter(description = "SQL query") @RequestParam("sql") String sqlQuery,
            @Parameter(description = "Maximum results to return") @RequestParam(value = "maxResults", defaultValue = "100") int maxResults) {
        try {
            List<DocumentPropertiesDTO> results = documentService.searchDocuments(sqlQuery, maxResults);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Search documents (Advanced)", 
              description = "Advanced search for documents with filtering, pagination, and sorting")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = DocumentPropertiesDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search request")
    })
    @PostMapping("/search")
    public ResponseEntity<?> searchDocumentsAdvanced(
            @Parameter(description = "Search request parameters") @RequestBody SearchRequestDTO searchRequest) {
        try {
            List<DocumentPropertiesDTO> results = documentService.searchDocumentsAdvanced(searchRequest);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}