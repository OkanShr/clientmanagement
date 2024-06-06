package com.xera.clientmanagement.service;

import com.xera.clientmanagement.entity.ClientImages;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientImageService {
    void uploadClientImage(Long clientId, MultipartFile file, String bearerToken);

    List<ClientImages> getClientImages(Long clientId);

    void deleteClientImage(Long clientId, Long imageId);
}