package com.xera.clientmanagement.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.xera.clientmanagement.Filestore.FileStore;
import com.xera.clientmanagement.entity.Client;
import com.xera.clientmanagement.entity.ClientImages;
import com.xera.clientmanagement.repository.ClientImageRepository;
import com.xera.clientmanagement.repository.ClientRepository;
import com.xera.clientmanagement.service.ClientImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ClientImageServiceImpl implements ClientImageService {

    private final AmazonS3 amazonS3;
    private final FileStore fileStore;
    private final ClientRepository clientRepository;
    private final ClientImageRepository clientImageRepository;

    @Autowired
    public ClientImageServiceImpl(AmazonS3 amazonS3, FileStore fileStore, ClientRepository clientRepository, ClientImageRepository clientImageRepository) {
        this.amazonS3 = amazonS3;
        this.fileStore = fileStore;
        this.clientRepository = clientRepository;
        this.clientImageRepository = clientImageRepository;
    }

    @Override
    public List<ClientImages> getClientImages(Long clientId) {
        // Retrieve client images metadata from your database/repository
        List<ClientImages> clientImagesMetadata = clientImageRepository.findByClient_ClientId(clientId);

        // Retrieve images from Amazon S3 based on the metadata
        List<ClientImages> clientImages = new ArrayList<>();
        for (ClientImages clientImageMetadata : clientImagesMetadata) {
            // Create a new ClientImages object
            ClientImages clientImage = new ClientImages();
            // Set the ID
            clientImage.setId(clientImageMetadata.getId());
            clientImage.setFileName(clientImageMetadata.getFileName()); // Set the file name

            // Set the client object
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Client not found"));
            clientImage.setClient(client);

            // Construct the key using clientId
            String key =  clientId + "/images/" + clientImageMetadata.getFileName(); // Use clientId in the key

            // Generate a pre-signed URL for the image
            String imageUrl = generatePresignedUrl(key); // Pass the key to generatePresignedUrl
            clientImage.setImageUrl(imageUrl); // Set the pre-signed URL as the image URL

            // Add the client image to the list
            clientImages.add(clientImage);
        }

        return clientImages;
    }

    private String generatePresignedUrl(String key) {
        // Generate a pre-signed URL for the image
        String bucketName = "xeramedimages"; // Bucket name
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // Expiry time: 1 hour from now

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        // Generate the pre-signed URL
        URL preSignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return preSignedUrl.toString();
    }


    @Override
    public void uploadClientImage(Long clientId, MultipartFile file, String bearerToken) {
        // Retrieve client from database
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Save image to file store
        String fileName = file.getOriginalFilename();
        try {
            fileStore.save(clientId, fileName, Optional.empty(), file.getInputStream(),
                    false, Optional.empty());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + fileName, e);
        }

        // Update client's image gallery
        ClientImages clientImage = new ClientImages();
        clientImage.setClient(client);
        clientImage.setFileName(fileName);
        clientImageRepository.save(clientImage); // Use clientImagesRepository here
    }


    @Override
    public void deleteClientImage(Long clientId, Long imageId) {
        // Retrieve the client and image metadata
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        ClientImages clientImage = clientImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (!clientImage.getClient().getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Image does not belong to the client");
        }

        // Construct the key using clientId and image file name
        String key = clientId + "/images/" + clientImage.getFileName();

        // Delete the image from S3
        amazonS3.deleteObject(new DeleteObjectRequest("xeramedimages", key));

        // Remove image metadata from the database
        clientImageRepository.delete(clientImage);
    }
}
