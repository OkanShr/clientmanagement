package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.entity.ClientPdf;
import com.xera.clientmanagement.service.ClientPdfService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/client-pdfs")
public class ClientPdfController {

    private final ClientPdfService clientPdfService;

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping("{clientId}/upload")
    public ResponseEntity<String> uploadClientPdf(@RequestParam("file") MultipartFile file,
                                                  @PathVariable("clientId") Long clientId,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bearer token missing");
        }

        String bearerToken = authorizationHeader.substring(7);

        try {
            clientPdfService.uploadClientPdf(clientId, file, bearerToken);
            String message = String.format("PDF '%s' uploaded successfully for client ID: %d", file.getOriginalFilename(), clientId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload PDF: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("{clientId}")
    public ResponseEntity<List<ClientPdf>> getClientPdfs(@PathVariable("clientId") Long clientId) {
        List<ClientPdf> clientPdfs = clientPdfService.getClientPdfs(clientId);
        return ResponseEntity.ok(clientPdfs);
    }


    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("{clientId}/delete/{pdfId}")
    public ResponseEntity<String> deleteClientImage(@PathVariable("clientId") Long clientId,
                                                    @PathVariable("pdfId") Long pdfId) {
        try {
            clientPdfService.deleteClientPdf(clientId, pdfId);
            return ResponseEntity.ok("Pdf deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete PDF: " + e.getMessage());
        }
    }
}