package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.entity.ClientImages;
import com.xera.clientmanagement.service.ClientImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/client-images")
public class ClientImageController {

    private final ClientImageService clientImageService;

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping("{clientId}/upload")
    public ResponseEntity<String> uploadClientImage(@RequestParam("file") MultipartFile file,
                                                    @PathVariable("clientId") Long clientId,
                                                    @RequestHeader("Authorization") String authorizationHeader) {
        // Check if the Authorization header contains a Bearer token
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bearer token missing");
        }

        // Extract the Bearer token
        String bearerToken = authorizationHeader.substring(7); // Remove "Bearer " prefix

        // Delegate file saving logic to service layer
        try {
            clientImageService.uploadClientImage(clientId, file, bearerToken);
            String message = String.format("File '%s' uploaded successfully for client ID: %d", file.getOriginalFilename(), clientId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }


    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("{clientId}")
    public ResponseEntity<List<ClientImages>> getClientImages(@PathVariable("clientId") Long clientId) {
        List<ClientImages> clientImages = clientImageService.getClientImages(clientId);
        return ResponseEntity.ok(clientImages);
    }


    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("{clientId}/delete/{imageId}")
    public ResponseEntity<String> deleteClientImage(@PathVariable("clientId") Long clientId,
                                                    @PathVariable("imageId") Long imageId) {
        try {
            clientImageService.deleteClientImage(clientId, imageId);
            return ResponseEntity.ok("Image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete image: " + e.getMessage());
        }
    }


}