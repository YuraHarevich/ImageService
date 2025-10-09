package ru.kharevich.imageservice.util.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.repository.ImageRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageValidationService {

    private final ImageRepository imageRepository;

    public Image findByIdThrowsExceptionIfDoesntExist(UUID id, RuntimeException exception) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> exception);
        return image;
    }

    public Image findByUrlThrowsExceptionIfDoesntExist(String url, RuntimeException exception) {
        Image image = imageRepository.findByUrl(url)
                .orElseThrow(() -> exception);
        return image;
    }

//    public void findByUsernameThrowsExceptionIfExists(String username, RuntimeException exception) {
//        Optional<User> userOpt = userRepository.findByUsername(username);
//        if (userOpt.isPresent()) {
//            throw new IllegalStateException(USER_ALREADY_EXISTS_MESSAGE);
//        }
//    }

}
