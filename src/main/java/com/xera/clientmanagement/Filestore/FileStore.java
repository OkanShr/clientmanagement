package com.xera.clientmanagement.Filestore;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Service
public class FileStore {

    private final AmazonS3 s3;

    @Autowired
    public FileStore(AmazonS3 s3) {
        this.s3 = s3;
    }

    public void save(Long clientId, String fileName, Optional<Map<String, String>> optionalMetadata,
                     InputStream inputStream, Boolean isAppointmentPdf, Optional<Long> optionalAppointmentId) {
        // Determine the folder based on the file type
        String folder;
        if (isImage(fileName)) {
            folder = "images";
        } else if (isPDF(fileName)) {
            folder = "pdfs";
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }

        // Construct the S3 path
        String s3Path;
        if (isAppointmentPdf) {
            Long appointmentId = optionalAppointmentId.orElseThrow(() -> new IllegalArgumentException("Appointment ID is required for appointment PDFs"));
            s3Path = clientId + "/" + folder + "/" + appointmentId + "/" + fileName;
        } else {
            s3Path = clientId + "/" + folder + "/" + fileName;
        }

        // Upload the object to Amazon S3
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            optionalMetadata.ifPresent(map -> {
                if (!map.isEmpty()) {
                    map.forEach(metadata::addUserMetadata);
                }
            });
            metadata.setContentLength(inputStream.available());
            s3.putObject("xeramedimages", s3Path, inputStream, metadata);
        } catch (IOException | SdkClientException e) {
            throw new IllegalStateException("Failed to store file to S3: " + e.getMessage(), e);
        } finally {
            try {
                // Close the input stream
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                // Log or handle the exception
                ex.printStackTrace();
            }
        }
    }

    // Helper method to check if the file is an image
    private boolean isImage(String fileName) {
        System.out.println("Checking file extension...");
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".png");
    }

    // Helper method to check if the file is a PDF
    private boolean isPDF(String fileName) {
        System.out.println("Checking file extension...");
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".pdf") || lowerCaseName.endsWith(".docx")|| lowerCaseName.endsWith(".doc");
    }
}
