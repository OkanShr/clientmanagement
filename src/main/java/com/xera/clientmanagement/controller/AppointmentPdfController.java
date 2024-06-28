package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.entity.AppointmentPdf;
import com.xera.clientmanagement.entity.PdfFile;
import com.xera.clientmanagement.service.AppointmentPdfService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/appointment-pdfs")
public class AppointmentPdfController {

    private final AppointmentPdfService appointmentPdfService;

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping("{clientId}/{appointmentId}/upload")
    public ResponseEntity<String> uploadAppointmentPdf(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("type") String type,
                                                       @PathVariable("clientId") Long clientId,
                                                       @PathVariable("appointmentId") Long appointmentId,
                                                       @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bearer token missing");
        }

        String bearerToken = authorizationHeader.substring(7);

        try {
            appointmentPdfService.uploadAppointmentPdf(appointmentId, clientId, file, type, bearerToken);
            String message = String.format("PDF '%s' uploaded successfully for appointment ID: %d",
                    file.getOriginalFilename(), appointmentId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload PDF: " +
                    e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("{clientId}/{appointmentId}")
    public ResponseEntity<List<PdfFile>> getAppointmentPdfs(@PathVariable("appointmentId") Long appointmentId,
                                                            @PathVariable("clientId") Long clientId) {
        List<PdfFile> appointmentPdfs = appointmentPdfService.getAppointmentPdfs(appointmentId, clientId);
        return ResponseEntity.ok(appointmentPdfs);
    }

    @PreAuthorize("hasAuthority('USER', 'ADMIN')")
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<PdfFile>> getAllAppointmentPdfsByClientId(@PathVariable("clientId") Long clientId) {
        List<PdfFile> appointmentPdfs = appointmentPdfService.getAllAppointmentPdfsByClientId(clientId);
        return ResponseEntity.ok(appointmentPdfs);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("{clientId}/{appointmentId}/delete/{pdfId}")
    public ResponseEntity<String> deleteAppointmentPdf(@PathVariable("appointmentId") Long appointmentId,
                                                       @PathVariable("clientId") Long clientId,
                                                       @PathVariable("pdfId") Long pdfId) {
        try {
            appointmentPdfService.deleteAppointmentPdf(appointmentId, clientId, pdfId);
            return ResponseEntity.ok("PDF deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete PDF: " + e.getMessage());
        }
    }
}
