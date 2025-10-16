package ru.kharevich.imageservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.response.PageableResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.exception.ImageNotFoundException;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.model.ImageType;
import ru.kharevich.imageservice.repository.ImageRepository;
import ru.kharevich.imageservice.util.PageUtils;
import ru.kharevich.imageservice.util.mapper.ImageMapper;
import ru.kharevich.imageservice.util.mapper.PageMapper;
import ru.kharevich.imageservice.util.validation.ImageValidationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private ImageValidationService imageValidationService;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    void getById_ShouldReturnImageResponse_WhenImageExists() {
        UUID id = UUID.randomUUID();
        Image image = createTestImage(id);
        byte[] fileBytes = "test file content".getBytes();
        ImageResponse expectedResponse = new ImageResponse(
                ImageType.AVATAR,
                List.of(new FileTransferEntity(fileBytes, "test-image.png")),
                id
        );

        when(imageValidationService.findByIdThrowsExceptionIfDoesntExist(eq(id), any(ImageNotFoundException.class)))
                .thenReturn(image);
        when(s3StorageService.downloadFile(image.getName())).thenReturn(fileBytes);
        when(imageMapper.toResponse(
                eq(image.getImageType()),
                eq(Collections.singletonList(fileBytes)),
                eq(Collections.singletonList(image.getName())),
                eq(image.getParentEntityId())
        )).thenReturn(expectedResponse);

        ImageResponse result = imageService.getById(id);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(imageValidationService).findByIdThrowsExceptionIfDoesntExist(eq(id), any(ImageNotFoundException.class));
        verify(s3StorageService).downloadFile(image.getName());
    }

    @Test
    void getByUrl_ShouldReturnImageResponse_WhenImageExists() {
        String url = "http://example.com/image.png";
        UUID id = UUID.randomUUID();
        Image image = createTestImage(id);
        image.setUrl(url);
        byte[] fileBytes = "test file content".getBytes();
        ImageResponse expectedResponse = new ImageResponse(
                ImageType.AVATAR,
                List.of(new FileTransferEntity(fileBytes, "test-image.png")),
                id
        );

        when(imageValidationService.findByUrlThrowsExceptionIfDoesntExist(eq(url), any(ImageNotFoundException.class)))
                .thenReturn(image);
        when(s3StorageService.downloadFile(image.getName())).thenReturn(fileBytes);
        when(imageMapper.toResponse(
                eq(image.getImageType()),
                eq(Collections.singletonList(fileBytes)),
                eq(Collections.singletonList(image.getName())),
                eq(image.getParentEntityId())
        )).thenReturn(expectedResponse);

        ImageResponse result = imageService.getByUrl(url);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(imageValidationService).findByUrlThrowsExceptionIfDoesntExist(eq(url), any(ImageNotFoundException.class));
        verify(s3StorageService).downloadFile(image.getName());
    }

    @Test
    void deleteById_ShouldDeleteImage_WhenImageExists() {
        UUID id = UUID.randomUUID();
        Image image = createTestImage(id);

        when(imageValidationService.findByIdThrowsExceptionIfDoesntExist(eq(id), any(ImageNotFoundException.class)))
                .thenReturn(image);
        doNothing().when(imageRepository).deleteById(id);
        doNothing().when(s3StorageService).deleteFile(image.getName());

        imageService.deleteById(id);

        verify(imageValidationService).findByIdThrowsExceptionIfDoesntExist(eq(id), any(ImageNotFoundException.class));
        verify(imageRepository).deleteById(id);
        verify(s3StorageService).deleteFile(image.getName());
    }


    @Test
    void getByParentId_ShouldReturnImageResponse_WhenImagesExist() {
        UUID parentId = UUID.randomUUID();
        List<Image> images = List.of(
                createTestImage(UUID.randomUUID(), parentId, "image1.png"),
                createTestImage(UUID.randomUUID(), parentId, "image2.png")
        );

        byte[] fileBytes1 = "file1 content".getBytes();
        byte[] fileBytes2 = "file2 content".getBytes();

        ImageResponse expectedResponse = new ImageResponse(
                ImageType.AVATAR,
                List.of(
                        new FileTransferEntity(fileBytes1, "image1.png"),
                        new FileTransferEntity(fileBytes2, "image2.png")
                ),
                parentId
        );

        when(imageRepository.findByParentEntityId(parentId)).thenReturn(images);
        when(s3StorageService.downloadFile("image1.png")).thenReturn(fileBytes1);
        when(s3StorageService.downloadFile("image2.png")).thenReturn(fileBytes2);
        when(imageMapper.toResponse(
                eq(images.get(0).getImageType()),
                eq(List.of(fileBytes1, fileBytes2)),
                eq(List.of("image1.png", "image2.png")),
                eq(parentId)
        )).thenReturn(expectedResponse);

        ImageResponse result = imageService.getByParentId(parentId);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(imageRepository).findByParentEntityId(parentId);
        verify(s3StorageService, times(2)).downloadFile(anyString());
    }

    @Test
    void getByParentId_ShouldThrowException_WhenNoImagesFound() {
        UUID parentId = UUID.randomUUID();
        when(imageRepository.findByParentEntityId(parentId)).thenReturn(Collections.emptyList());

        assertThrows(ImageNotFoundException.class, () -> imageService.getByParentId(parentId));
        verify(imageRepository).findByParentEntityId(parentId);
        verify(s3StorageService, never()).downloadFile(anyString());
    }

    @Test
    void getManyByParentId_ShouldReturnPageableResponse() {
        List<UUID> parentIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        int pageNumber = 0;
        int size = 10;

        ImageResponse response1 = new ImageResponse(ImageType.AVATAR, List.of(), parentIds.get(0));
        ImageResponse response2 = new ImageResponse(ImageType.AVATAR, List.of(), parentIds.get(1));

        List<ImageResponse> responses = List.of(response1, response2);
        Page<ImageResponse> page = new PageImpl<>(responses);
        PageableResponse<ImageResponse> expectedResponse = new PageableResponse<>(
                2L, 1, 0, 10, responses
        );

        ImageServiceImpl imageServiceSpy = spy(imageService);

        doReturn(response1).when(imageServiceSpy).getByParentId(parentIds.get(0));
        doReturn(response2).when(imageServiceSpy).getByParentId(parentIds.get(1));

        try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class)) {
            pageUtilsMock.when(() -> PageUtils.convertListToPage(responses, pageNumber, size))
                    .thenReturn(page);

            when(pageMapper.toResponse(page)).thenReturn(expectedResponse);

            PageableResponse<ImageResponse> result = imageServiceSpy.getManyByParentId(parentIds, pageNumber, size);

            assertNotNull(result);
            assertEquals(expectedResponse, result);
            verify(imageServiceSpy, times(2)).getByParentId(any(UUID.class));
            verify(pageMapper).toResponse(page);
        }
    }

    @Test
    void save_ShouldSaveImageAndReturnResponse_WhenValidRequest() throws IOException {
        UUID parentId = UUID.randomUUID();
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);

        ImageRequest imageRequest = new ImageRequest(
                ImageType.AVATAR,
                parentId,
                List.of(file1, file2)
        );

        when(file1.getOriginalFilename()).thenReturn("image1.jpg");
        when(file2.getOriginalFilename()).thenReturn("image2.jpg");
        when(file1.getBytes()).thenReturn("file1 content".getBytes());
        when(file2.getBytes()).thenReturn("file2 content".getBytes());

        String expectedName1 = "image1.jpg-" + parentId;
        String expectedName2 = "image2.jpg-" + parentId;
        String expectedUrl1 = "http://s3/bucket/image1.jpg-" + parentId;
        String expectedUrl2 = "http://s3/bucket/image2.jpg-" + parentId;

        when(s3StorageService.uploadFile(file1, expectedName1)).thenReturn(expectedUrl1);
        when(s3StorageService.uploadFile(file2, expectedName2)).thenReturn(expectedUrl2);

        Image image1 = createTestImage(UUID.randomUUID());
        Image image2 = createTestImage(UUID.randomUUID());

        when(imageMapper.toEntity(imageRequest, expectedUrl1, expectedName1)).thenReturn(image1);
        when(imageMapper.toEntity(imageRequest, expectedUrl2, expectedName2)).thenReturn(image2);
        when(imageRepository.saveAndFlush(any(Image.class))).thenReturn(image1).thenReturn(image2);

        List<byte[]> expectedBytes = List.of("file1 content".getBytes(), "file2 content".getBytes());
        List<String> expectedNames = List.of(expectedName1, expectedName2);
        ImageResponse expectedResponse = new ImageResponse(
                ImageType.AVATAR,
                List.of(
                        new FileTransferEntity("file1 content".getBytes(), expectedName1),
                        new FileTransferEntity("file2 content".getBytes(), expectedName2)
                ),
                parentId
        );

        when(imageMapper.toResponse(
                eq(ImageType.AVATAR),
                any(List.class),
                any(List.class),
                eq(parentId)
        )).thenReturn(expectedResponse);

        ImageResponse result = imageService.save(imageRequest);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(s3StorageService, times(2)).uploadFile(any(MultipartFile.class), anyString());
        verify(imageRepository, times(2)).saveAndFlush(any(Image.class));
        verify(imageMapper).toResponse(
                eq(ImageType.AVATAR),
                any(List.class),
                any(List.class),
                eq(parentId)
        );
    }

    @Test
    void deleteByParentId_ShouldDeleteAllImagesForParent() {
        UUID parentId = UUID.randomUUID();
        List<Image> images = List.of(
                createTestImage(UUID.randomUUID(), parentId, "image1.png"),
                createTestImage(UUID.randomUUID(), parentId, "image2.png")
        );

        when(imageRepository.findByParentEntityId(parentId)).thenReturn(images);
        doNothing().when(imageRepository).deleteById(any(UUID.class));
        doNothing().when(s3StorageService).deleteFile(anyString());

        imageService.deleteByParentId(parentId);

        verify(imageRepository).findByParentEntityId(parentId);
        verify(imageRepository, times(2)).deleteById(any(UUID.class));
        verify(s3StorageService, times(2)).deleteFile(anyString());
    }

    @Test
    void getSvgIcons_ShouldReturnFileTransferEntities() {
        List<FileTransferEntity> expectedIcons = List.of(
                new FileTransferEntity("icon1".getBytes(), "icon1.svg"),
                new FileTransferEntity("icon2".getBytes(), "icon2.svg")
        );

        when(s3StorageService.downloadSvgIcons()).thenReturn(expectedIcons);

        List<FileTransferEntity> result = imageService.getSvgIcons();

        assertNotNull(result);
        assertEquals(expectedIcons, result);
        verify(s3StorageService).downloadSvgIcons();
    }

    private Image createTestImage(UUID id) {
        return createTestImage(id, UUID.randomUUID(), "test-image.png");
    }

    private Image createTestImage(UUID id, UUID parentId, String name) {
        return Image.builder()
                .id(id)
                .url("http://example.com/" + name)
                .name(name)
                .parentEntityId(parentId)
                .imageType(ImageType.AVATAR)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

}