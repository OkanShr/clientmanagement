package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.entity.AppointmentPdf;
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
    @PostMapping("{appointmentId}/upload")
    public ResponseEntity<String> uploadAppointmentPdf(@RequestParam("file") MultipartFile file,
                                                       @PathVariable("appointmentId") Long appointmentId,
                                                       @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bearer token missing");
        }

        String bearerToken = authorizationHeader.substring(7);

        try {
            appointmentPdfService.uploadAppointmentPdf(appointmentId, file, bearerToken);
            String message = String.format("PDF '%s' uploaded successfully for appointment ID: %d", file.getOriginalFilename(), appointmentId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload PDF: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("{appointmentId}")
    public ResponseEntity<List<AppointmentPdf>> getAppointmentPdfs(@PathVariable("appointmentId") Long appointmentId) {
        List<AppointmentPdf> appointmentPdfs = appointmentPdfService.getAppointmentPdfs(appointmentId);
        return ResponseEntity.ok(appointmentPdfs);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("{appointmentId}/delete/{pdfId}")
    public ResponseEntity<String> deleteAppointmentPdf(@PathVariable("appointmentId") Long appointmentId,
                                                       @PathVariable("pdfId") Long pdfId) {
        try {
            appointmentPdfService.deleteAppointmentPdf(appointmentId, pdfId);
            return ResponseEntity.ok("PDF deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete PDF: " + e.getMessage());
        }
    }
}
