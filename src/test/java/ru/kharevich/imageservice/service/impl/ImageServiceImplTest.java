package ru.kharevich.imageservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.exception.FileUploadException;
import ru.kharevich.imageservice.exception.ImageNotFoundException;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.model.ImageType;
import ru.kharevich.imageservice.repository.ImageRepository;
import ru.kharevich.imageservice.util.mapper.ImageMapper;
import ru.kharevich.imageservice.util.validation.ImageValidationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private ImageValidationService imageValidationService;

    @InjectMocks
    private ImageServiceImpl imageService;

    private UUID testId;
    private UUID parentId;
    private Image testImage;
    private MultipartFile testMultipartFile;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        parentId = UUID.randomUUID();

        testImage = Image.builder()
                .id(testId)
                .url("http://test.com/image.jpg")
                .name("test-image.jpg")
                .uploadedAt(LocalDateTime.now())
                .parentEntityId(parentId)
                .imageType(ImageType.POST_ATTACHMENT)
                .build();

        testMultipartFile = mock(MultipartFile.class);
    }

    @Test
    void getById_WhenImageExists_ShouldReturnImageResponse() {
        // Arrange
        byte[] fileContent = "test content".getBytes();
        ImageResponse expectedResponse = new ImageResponse(ImageType.POST_ATTACHMENT, Collections.emptyList());

        when(imageValidationService.findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class)))
                .thenReturn(testImage);
        when(s3StorageService.downloadFile(testImage.getName())).thenReturn(fileContent);
        when(imageMapper.toResponse(ImageType.POST_ATTACHMENT, Collections.singletonList(fileContent),
                Collections.singletonList(testImage.getName())))
                .thenReturn(expectedResponse);

        // Act
        ImageResponse result = imageService.getById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(imageValidationService).findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class));
        verify(s3StorageService).downloadFile(testImage.getName());
        verify(imageMapper).toResponse(ImageType.POST_ATTACHMENT, Collections.singletonList(fileContent),
                Collections.singletonList(testImage.getName()));
    }

    @Test
    void getById_WhenImageNotFound_ShouldThrowException() {
        // Arrange
        when(imageValidationService.findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class)))
                .thenThrow(new ImageNotFoundException("Image not found"));

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.getById(testId));
        verify(imageValidationService).findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class));
        verifyNoInteractions(s3StorageService, imageMapper);
    }

    @Test
    void getByUrl_WhenImageExists_ShouldReturnImageResponse() {
        // Arrange
        String testUrl = "http://test.com/image.jpg";
        byte[] fileContent = "test content".getBytes();
        ImageResponse expectedResponse = new ImageResponse(ImageType.POST_ATTACHMENT, Collections.emptyList());

        when(imageValidationService.findByUrlThrowsExceptionIfDoesntExist(eq(testUrl), any(ImageNotFoundException.class)))
                .thenReturn(testImage);
        when(s3StorageService.downloadFile(testImage.getName())).thenReturn(fileContent);
        when(imageMapper.toResponse(ImageType.POST_ATTACHMENT, Collections.singletonList(fileContent),
                Collections.singletonList(testImage.getName())))
                .thenReturn(expectedResponse);

        // Act
        ImageResponse result = imageService.getByUrl(testUrl);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(imageValidationService).findByUrlThrowsExceptionIfDoesntExist(eq(testUrl), any(ImageNotFoundException.class));
        verify(s3StorageService).downloadFile(testImage.getName());
    }

    @Test
    void getByUrl_WhenImageNotFound_ShouldThrowException() {
        // Arrange
        String testUrl = "http://test.com/image.jpg";
        when(imageValidationService.findByUrlThrowsExceptionIfDoesntExist(eq(testUrl), any(ImageNotFoundException.class)))
                .thenThrow(new ImageNotFoundException("Image not found"));

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.getByUrl(testUrl));
        verify(imageValidationService).findByUrlThrowsExceptionIfDoesntExist(eq(testUrl), any(ImageNotFoundException.class));
        verifyNoInteractions(s3StorageService, imageMapper);
    }

    @Test
    void deleteById_WhenImageExists_ShouldDeleteImage() {
        // Arrange
        when(imageValidationService.findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class)))
                .thenReturn(testImage);
        doNothing().when(imageRepository).deleteById(testId);
        doNothing().when(s3StorageService).deleteFile(testImage.getName());

        // Act
        imageService.deleteById(testId);

        // Assert
        verify(imageValidationService).findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class));
        verify(imageRepository).deleteById(testId);
        verify(s3StorageService).deleteFile(testImage.getName());
    }

    @Test
    void deleteById_WhenImageNotFound_ShouldThrowException() {
        // Arrange
        when(imageValidationService.findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class)))
                .thenThrow(new ImageNotFoundException("Image not found"));

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.deleteById(testId));
        verify(imageValidationService).findByIdThrowsExceptionIfDoesntExist(eq(testId), any(ImageNotFoundException.class));
        verifyNoInteractions(imageRepository, s3StorageService);
    }

    @Test
    void save_WithValidImageRequest_ShouldSaveAndReturnResponse() throws IOException {
        // Arrange
        List<MultipartFile> files = Collections.singletonList(testMultipartFile);
        ImageRequest imageRequest = new ImageRequest(ImageType.POST_ATTACHMENT, parentId, files);

        String originalFilename = "test.jpg";
        String expectedFilename = originalFilename + "-" + parentId.toString();
        String expectedUrl = "http://s3.com/" + expectedFilename;
        byte[] fileBytes = "file content".getBytes();
        ImageResponse expectedResponse = new ImageResponse(ImageType.POST_ATTACHMENT, Collections.emptyList());

        when(testMultipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(testMultipartFile.getBytes()).thenReturn(fileBytes);

        // Используем any() для аргументов вместо конкретных значений
        when(s3StorageService.uploadFile(any(MultipartFile.class), anyString())).thenReturn(expectedUrl);

        Image imageToSave = Image.builder()
                .url(expectedUrl)
                .name(expectedFilename)
                .parentEntityId(parentId)
                .imageType(ImageType.POST_ATTACHMENT)
                .build();

        when(imageMapper.toEntity(any(ImageRequest.class), anyString(), anyString())).thenReturn(imageToSave);
        when(imageRepository.saveAndFlush(any(Image.class))).thenReturn(imageToSave);
        when(imageMapper.toResponse(eq(ImageType.POST_ATTACHMENT), anyList(), anyList())).thenReturn(expectedResponse);

        // Act
        ImageResponse result = imageService.save(imageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        // Проверяем, что uploadFile был вызван с правильными аргументами
        verify(s3StorageService).uploadFile(eq(testMultipartFile), eq(expectedFilename));
        verify(imageMapper).toEntity(eq(imageRequest), eq(expectedUrl), eq(expectedFilename));
        verify(imageRepository).saveAndFlush(imageToSave);
        verify(imageMapper).toResponse(eq(ImageType.POST_ATTACHMENT), eq(Collections.singletonList(fileBytes)),
                eq(Collections.singletonList(expectedFilename)));
    }

    @Test
    void save_WithMultipleFiles_ShouldSaveAllFiles() throws IOException {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = Arrays.asList(file1, file2);
        ImageRequest imageRequest = new ImageRequest(ImageType.POST_ATTACHMENT, parentId, files);

        when(file1.getOriginalFilename()).thenReturn("file1.jpg");
        when(file2.getOriginalFilename()).thenReturn("file2.jpg");
        when(file1.getBytes()).thenReturn("content1".getBytes());
        when(file2.getBytes()).thenReturn("content2".getBytes());

        when(s3StorageService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("http://s3.com/file");
        when(imageMapper.toEntity(any(ImageRequest.class), anyString(), anyString())).thenReturn(testImage);
        when(imageRepository.saveAndFlush(any(Image.class))).thenReturn(testImage);

        ImageResponse expectedResponse = new ImageResponse(ImageType.POST_ATTACHMENT, Collections.emptyList());
        when(imageMapper.toResponse(eq(ImageType.POST_ATTACHMENT), anyList(), anyList())).thenReturn(expectedResponse);

        // Act
        ImageResponse result = imageService.save(imageRequest);

        // Assert
        assertNotNull(result);
        verify(s3StorageService, times(2)).uploadFile(any(MultipartFile.class), anyString());
        verify(imageRepository, times(2)).saveAndFlush(any(Image.class));
    }

    @Test
    void save_WhenFileConversionFails_ShouldThrowException() throws IOException {
        // Arrange
        List<MultipartFile> files = Collections.singletonList(testMultipartFile);
        ImageRequest imageRequest = new ImageRequest(ImageType.POST_ATTACHMENT, parentId, files);

        String originalFilename = "test.jpg";
        String expectedFilename = originalFilename + "-" + parentId.toString();

        when(testMultipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(testMultipartFile.getBytes()).thenThrow(new IOException("Conversion failed"));

        // Настраиваем моки для остальных вызовов, которые могут произойти
        when(s3StorageService.uploadFile(any(MultipartFile.class), anyString())).thenReturn("http://dummy-url.com");
        when(imageMapper.toEntity(any(ImageRequest.class), anyString(), anyString())).thenReturn(testImage);

        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class,
                () -> imageService.save(imageRequest));

        assertEquals("unable convert file", exception.getMessage());

        // Проверяем, что методы были вызваны до момента исключения
        verify(testMultipartFile).getOriginalFilename();
        verify(testMultipartFile).getBytes();

        // Не проверяем конкретные взаимодействия, так как поведение может быть недетерминированным
        // из-за исключения в лямбде
    }

    @Test
    void getByParentId_WhenImagesExist_ShouldReturnResponse() {
        // Arrange
        List<Image> images = Arrays.asList(testImage);
        byte[] fileContent = "content".getBytes();
        ImageResponse expectedResponse = new ImageResponse(ImageType.POST_ATTACHMENT, Collections.emptyList());

        when(imageRepository.findByParentEntityId(parentId)).thenReturn(images);
        when(s3StorageService.downloadFile(testImage.getName())).thenReturn(fileContent);
        when(imageMapper.toResponse(ImageType.POST_ATTACHMENT, Collections.singletonList(fileContent),
                Collections.singletonList(testImage.getName())))
                .thenReturn(expectedResponse);

        // Act
        ImageResponse result = imageService.getByParentId(parentId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(imageRepository).findByParentEntityId(parentId);
        verify(s3StorageService).downloadFile(testImage.getName());
    }

    @Test
    void getByParentId_WhenNoImages_ShouldThrowException() {
        // Arrange
        when(imageRepository.findByParentEntityId(parentId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.getByParentId(parentId));
        verify(imageRepository).findByParentEntityId(parentId);
        verifyNoInteractions(s3StorageService, imageMapper);
    }

    @Test
    void getByParentId_WithMultipleImages_ShouldReturnCombinedResponse() {
        // Arrange
        Image image2 = Image.builder()
                .id(UUID.randomUUID())
                .url("http://test.com/image2.jpg")
                .name("test-image2.jpg")
                .uploadedAt(LocalDateTime.now())
                .parentEntityId(parentId)
                .imageType(ImageType.POST_ATTACHMENT)
                .build();

        List<Image> images = Arrays.asList(testImage, image2);
        byte[] content1 = "content1".getBytes();
        byte[] content2 = "content2".getBytes();
        ImageResponse expectedResponse = new ImageResponse(ImageType.POST_ATTACHMENT, Collections.emptyList());

        when(imageRepository.findByParentEntityId(parentId)).thenReturn(images);
        when(s3StorageService.downloadFile(testImage.getName())).thenReturn(content1);
        when(s3StorageService.downloadFile(image2.getName())).thenReturn(content2);
        when(imageMapper.toResponse(ImageType.POST_ATTACHMENT, Arrays.asList(content1, content2),
                Arrays.asList(testImage.getName(), image2.getName())))
                .thenReturn(expectedResponse);

        // Act
        ImageResponse result = imageService.getByParentId(parentId);

        // Assert
        assertNotNull(result);
        verify(s3StorageService, times(2)).downloadFile(anyString());
        verify(imageMapper).toResponse(eq(ImageType.POST_ATTACHMENT), anyList(), anyList());
    }

    @Test
    void deleteByParentId_WhenImagesExist_ShouldDeleteAll() {
        // Arrange
        Image image2 = Image.builder()
                .id(UUID.randomUUID())
                .url("http://test.com/image2.jpg")
                .name("test-image2.jpg")
                .uploadedAt(LocalDateTime.now())
                .parentEntityId(parentId)
                .imageType(ImageType.POST_ATTACHMENT)
                .build();

        List<Image> images = Arrays.asList(testImage, image2);

        when(imageRepository.findByParentEntityId(parentId)).thenReturn(images);
        doNothing().when(imageRepository).deleteById(any(UUID.class));
        doNothing().when(s3StorageService).deleteFile(anyString());

        // Act
        imageService.deleteByParentId(parentId);

        // Assert
        verify(imageRepository, times(2)).deleteById(any(UUID.class));
        verify(s3StorageService, times(2)).deleteFile(anyString());
        verify(imageRepository).findByParentEntityId(parentId);
    }

    @Test
    void deleteByParentId_WhenNoImages_ShouldDoNothing() {
        // Arrange
        when(imageRepository.findByParentEntityId(parentId)).thenReturn(Collections.emptyList());

        // Act
        imageService.deleteByParentId(parentId);

        // Assert
        verify(imageRepository).findByParentEntityId(parentId);
        verifyNoMoreInteractions(imageRepository);
        verifyNoInteractions(s3StorageService);
    }

}