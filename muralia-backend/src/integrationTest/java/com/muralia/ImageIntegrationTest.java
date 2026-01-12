package com.muralia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muralia.api.model.Image;
import com.muralia.api.model.ImageListResponse;
import com.muralia.entity.CustomerEntity;
import com.muralia.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the complete flow of image management with PostgreSQL storage.
 */
@DisplayName("Image Management Integration Tests")
class ImageIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CustomerEntity testCustomer;
    private String authToken;

    // Test fixtures
    private static final String TEST_IMAGE_TITLE = "Test Image";
    private static final String TEST_IMAGE_DESCRIPTION = "This is a test image";
    private static final String TEST_IMAGE_FILENAME = "sample-001.jpg";
    private static final String TEST_IMAGE_MIME_TYPE = "image/jpeg";
    private static final String TEST_PASSWORD = "Test123!";

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.deleteAll();
        testCustomer = createTestCustomer("testuser", "test@example.com");
        authToken = obtainAuthToken("test@example.com", TEST_PASSWORD);
    }

    @Nested
    @DisplayName("Image Upload")
    class ImageUploadTests {

        @Test
        @DisplayName("should successfully upload an image and return metadata")
        void shouldUploadImageSuccessfully() throws Exception {
            // given
            byte[] imageData = loadRealJpegImage();
            MockMultipartFile imageFile = createImageFile(TEST_IMAGE_FILENAME, imageData);

            // when
            ResultActions result = performImageUpload(imageFile, TEST_IMAGE_TITLE, TEST_IMAGE_DESCRIPTION);

            // then
            Image uploadedImage = extractImageFromResponse(result);
            assertImageMetadata(uploadedImage, imageData.length);
            assertImageUrl(uploadedImage);
        }

        @Test
        @DisplayName("should reject non-image files")
        void shouldRejectNonImageFiles() throws Exception {
            // given
            MockMultipartFile textFile = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "This is not an image".getBytes(StandardCharsets.UTF_8)
            );

            // when / then
            mockMvc.perform(multipart("/api/images")
                            .file(textFile)
                            .param("title", "Text File")
                            .header("Authorization", "Bearer " + authToken)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should reject empty files")
        void shouldRejectEmptyFile() throws Exception {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.png",
                    TEST_IMAGE_MIME_TYPE,
                    new byte[0]
            );

            // when / then
            mockMvc.perform(multipart("/api/images")
                            .file(emptyFile)
                            .param("title", "Empty File")
                            .header("Authorization", "Bearer " + authToken)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("should successfully upload a real JPEG image from resources")
        void shouldUploadRealJpegImage() throws Exception {
            // given
            MockMultipartFile realImageFile = createRealImageFile();
            byte[] imageData = loadRealJpegImage();

            // when
            ResultActions result = mockMvc.perform(multipart("/api/images")
                            .file(realImageFile)
                            .param("title", "Real Sample Image")
                            .param("description", "A real 884KB JPEG image for testing")
                            .header("Authorization", "Bearer " + authToken)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.url").exists());

            // then
            Image uploadedImage = extractImageFromResponse(result);
            assertThat(uploadedImage.getId()).isNotNull();
            assertThat(uploadedImage.getFileName()).isEqualTo("sample-001.jpg");
            assertThat(uploadedImage.getMimeType()).isEqualTo("image/jpeg");
            assertThat(uploadedImage.getTitle()).isEqualTo("Real Sample Image");
            assertThat(uploadedImage.getDescription()).isEqualTo("A real 884KB JPEG image for testing");
            assertThat(uploadedImage.getFileSize()).isEqualTo(imageData.length);
            assertThat(uploadedImage.getUrl().toString()).contains("/api/images/" + uploadedImage.getId() + "/file");

            // when - retrieve the uploaded file
            MvcResult fileResult = mockMvc.perform(get("/api/images/{imageId}/file", uploadedImage.getId()))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/jpeg"))
                    .andReturn();

            // then - verify file content matches
            byte[] retrievedImageData = fileResult.getResponse().getContentAsByteArray();
            assertThat(retrievedImageData).isEqualTo(imageData);
            assertThat(retrievedImageData.length).isEqualTo(imageData.length);
        }
    }

    @Nested
    @DisplayName("Image Retrieval")
    class ImageRetrievalTests {

        @Test
        @DisplayName("should retrieve image metadata by ID")
        void shouldRetrieveImageMetadataById() throws Exception {
            // given
            UUID imageId = uploadTestImage(TEST_IMAGE_FILENAME, TEST_IMAGE_TITLE);

            // when
            ResultActions result = mockMvc.perform(get("/api/images/{imageId}", imageId));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(imageId.toString()))
                    .andExpect(jsonPath("$.fileName").value(TEST_IMAGE_FILENAME))
                    .andExpect(jsonPath("$.title").value(TEST_IMAGE_TITLE))
                    .andExpect(jsonPath("$.mimeType").value(TEST_IMAGE_MIME_TYPE))
                    .andExpect(jsonPath("$.customerUsername").value(testCustomer.getUsername()));
        }

        @Test
        @DisplayName("should retrieve the actual image file bytes")
        void shouldRetrieveImageFileBytes() throws Exception {
            // given
            byte[] originalImageData = loadRealJpegImage();
            UUID imageId = uploadTestImage(TEST_IMAGE_FILENAME, TEST_IMAGE_TITLE, originalImageData);

            // when
            MvcResult result = mockMvc.perform(get("/api/images/{imageId}/file", imageId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", TEST_IMAGE_MIME_TYPE))
                    .andReturn();

            // then
            byte[] retrievedImageData = result.getResponse().getContentAsByteArray();
            assertThat(retrievedImageData).isEqualTo(originalImageData);
        }

        @Test
        @DisplayName("should return error for non-existent image")
        void shouldReturnNotFoundForNonExistentImage() throws Exception {
            // given
            UUID nonExistentId = UUID.randomUUID();

            // when / then
            mockMvc.perform(get("/api/images/{imageId}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Image not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/images/{imageId}/file", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Image not found with id: " + nonExistentId));
        }
    }

    @Nested
    @DisplayName("Image Listing")
    class ImageListingTests {

        @Test
        @DisplayName("should list latest images with default pagination")
        void shouldListLatestImages() throws Exception {
            // given
            UUID imageId = uploadTestImage(TEST_IMAGE_FILENAME, TEST_IMAGE_TITLE);

            // when
            MvcResult result = mockMvc.perform(get("/api/images")
                            .param("limit", "10")
                            .param("offset", "0"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images").isArray())
                    .andExpect(jsonPath("$.images[0].id").value(imageId.toString()))
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.limit").value(10))
                    .andExpect(jsonPath("$.offset").value(0))
                    .andReturn();

            // then
            ImageListResponse listResponse = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ImageListResponse.class
            );
            assertThat(listResponse.getImages()).hasSize(1);
            assertThat(listResponse.getTotal()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return images in reverse chronological order")
        void shouldReturnImagesInReverseChronologicalOrder() throws Exception {
            // given
            uploadMultipleTestImages(3);

            // when
            MvcResult result = mockMvc.perform(get("/api/images")
                            .param("limit", "10")
                            .param("offset", "0"))
                    .andExpect(status().isOk())
                    .andReturn();

            // then
            ImageListResponse listResponse = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    ImageListResponse.class
            );
            assertThat(listResponse.getImages()).hasSize(3);
            assertThat(listResponse.getImages().get(0).getTitle()).isEqualTo("Test Image 3");
            assertThat(listResponse.getImages().get(1).getTitle()).isEqualTo("Test Image 2");
            assertThat(listResponse.getImages().get(2).getTitle()).isEqualTo("Test Image 1");
        }

        @Test
        @DisplayName("should handle pagination correctly")
        void shouldHandlePaginationCorrectly() throws Exception {
            // given
            uploadMultipleTestImages(5);

            // when - first page
            mockMvc.perform(get("/api/images")
                            .param("limit", "2")
                            .param("offset", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images.length()").value(2))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.limit").value(2))
                    .andExpect(jsonPath("$.offset").value(0));

            // when - second page
            mockMvc.perform(get("/api/images")
                            .param("limit", "2")
                            .param("offset", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images.length()").value(2))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.offset").value(2));

            // when - third page (last page with 1 item)
            mockMvc.perform(get("/api/images")
                            .param("limit", "2")
                            .param("offset", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images.length()").value(1))
                    .andExpect(jsonPath("$.total").value(5))
                    .andExpect(jsonPath("$.offset").value(4));
        }
    }

    @Nested
    @DisplayName("Complete Image Workflow")
    class CompleteWorkflowTests {

        @Test
        @DisplayName("should complete full workflow: upload, retrieve metadata, retrieve file, list images")
        void shouldCompleteFullImageWorkflow() throws Exception {
            // given
            byte[] originalImageData = loadRealJpegImage();
            MockMultipartFile imageFile = createImageFile(TEST_IMAGE_FILENAME, originalImageData);

            // when - upload image
            ResultActions uploadResult = performImageUpload(imageFile, TEST_IMAGE_TITLE, TEST_IMAGE_DESCRIPTION);
            Image uploadedImage = extractImageFromResponse(uploadResult);
            UUID imageId = uploadedImage.getId();

            // then - verify URL structure
            assertThat(uploadedImage.getUrl().toString()).contains("/api/images/" + imageId + "/file");

            // when - retrieve metadata
            mockMvc.perform(get("/api/images/{imageId}", imageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(imageId.toString()))
                    .andExpect(jsonPath("$.title").value(TEST_IMAGE_TITLE));

            // when - retrieve file
            MvcResult fileResult = mockMvc.perform(get("/api/images/{imageId}/file", imageId))
                    .andExpect(status().isOk())
                    .andReturn();

            // then - verify file content
            assertThat(fileResult.getResponse().getContentAsByteArray()).isEqualTo(originalImageData);

            // when - list images
            MvcResult listResult = mockMvc.perform(get("/api/images")
                            .param("limit", "10")
                            .param("offset", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images[0].id").value(imageId.toString()))
                    .andReturn();

            // then - verify list contains uploaded image
            ImageListResponse listResponse = objectMapper.readValue(
                    listResult.getResponse().getContentAsString(),
                    ImageListResponse.class
            );
            assertThat(listResponse.getImages()).hasSize(1);
            assertThat(listResponse.getImages().get(0).getId()).isEqualTo(imageId);
        }

        @Test
        @DisplayName("should complete full workflow with real JPEG image")
        void shouldCompleteFullWorkflowWithRealJpeg() throws Exception {
            // given
            MockMultipartFile realImageFile = createRealImageFile();
            byte[] originalImageData = loadRealJpegImage();

            // when - upload real JPEG image
            ResultActions uploadResult = mockMvc.perform(multipart("/api/images")
                            .file(realImageFile)
                            .param("title", "Beautiful Landscape")
                            .param("description", "A stunning landscape photo from our test resources")
                            .header("Authorization", "Bearer " + authToken)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fileName").value("sample-001.jpg"))
                    .andExpect(jsonPath("$.mimeType").value("image/jpeg"));

            Image uploadedImage = extractImageFromResponse(uploadResult);
            UUID imageId = uploadedImage.getId();

            // then - verify large file was stored correctly
            assertThat(uploadedImage.getFileSize()).isGreaterThan(800_000); // ~884KB
            assertThat(uploadedImage.getUrl().toString()).contains("/api/images/" + imageId + "/file");

            // when - retrieve metadata
            mockMvc.perform(get("/api/images/{imageId}", imageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fileName").value("sample-001.jpg"))
                    .andExpect(jsonPath("$.mimeType").value("image/jpeg"))
                    .andExpect(jsonPath("$.title").value("Beautiful Landscape"));

            // when - retrieve the actual JPEG file
            MvcResult fileResult = mockMvc.perform(get("/api/images/{imageId}/file", imageId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/jpeg"))
                    .andExpect(header().exists("Content-Length"))
                    .andReturn();

            // then - verify complete file integrity
            byte[] retrievedData = fileResult.getResponse().getContentAsByteArray();
            assertThat(retrievedData).isEqualTo(originalImageData);
            assertThat(retrievedData.length).isEqualTo(originalImageData.length);

            // when - list images and verify our JPEG is there
            MvcResult listResult = mockMvc.perform(get("/api/images")
                            .param("limit", "10")
                            .param("offset", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images[0].fileName").value("sample-001.jpg"))
                    .andReturn();

            ImageListResponse listResponse = objectMapper.readValue(
                    listResult.getResponse().getContentAsString(),
                    ImageListResponse.class
            );

            // then - verify list contains our JPEG with correct metadata
            assertThat(listResponse.getImages()).hasSize(1);
            Image listedImage = listResponse.getImages().get(0);
            assertThat(listedImage.getId()).isEqualTo(imageId);
            assertThat(listedImage.getFileName()).isEqualTo("sample-001.jpg");
            assertThat(listedImage.getMimeType()).isEqualTo("image/jpeg");
            assertThat(listedImage.getFileSize()).isEqualTo(originalImageData.length);
        }
    }

    // ========================================
    // Helper Methods & Fixtures
    // ========================================

    private CustomerEntity createTestCustomer(String username, String email) {
        CustomerEntity customer = CustomerEntity.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .firstName("Test")
                .lastName("User")
                .build();
        return customerRepository.save(customer);
    }

    private String obtainAuthToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        com.muralia.api.model.AuthResponse authResponse = objectMapper.readValue(
                responseJson,
                com.muralia.api.model.AuthResponse.class
        );

        return authResponse.getToken();
    }

    private MockMultipartFile createImageFile(String filename, byte[] imageData) {
        return new MockMultipartFile("file", filename, TEST_IMAGE_MIME_TYPE, imageData);
    }

    private ResultActions performImageUpload(MockMultipartFile file, String title, String description) throws Exception {
        return mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .param("title", title)
                        .param("description", description)
                        .header("Authorization", "Bearer " + authToken)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists());
    }

    private Image extractImageFromResponse(ResultActions resultActions) throws Exception {
        MvcResult result = resultActions.andReturn();
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, Image.class);
    }

    private void assertImageMetadata(Image image, long expectedFileSize) {
        assertThat(image.getId()).isNotNull();
        assertThat(image.getFileName()).isEqualTo(TEST_IMAGE_FILENAME);
        assertThat(image.getTitle()).isEqualTo(TEST_IMAGE_TITLE);
        assertThat(image.getDescription()).isEqualTo(TEST_IMAGE_DESCRIPTION);
        assertThat(image.getMimeType()).isEqualTo(TEST_IMAGE_MIME_TYPE);
        assertThat(image.getFileSize()).isEqualTo(expectedFileSize);
        assertThat(image.getCustomerId()).isEqualTo(testCustomer.getId());
        assertThat(image.getCustomerUsername()).isEqualTo(testCustomer.getUsername());
    }

    private void assertImageUrl(Image image) {
        String expectedUrl = "/api/images/" + image.getId() + "/file";
        assertThat(image.getUrl().toString()).contains(expectedUrl);
    }

    private UUID uploadTestImage(String filename, String title) throws Exception {
        byte[] imageData = loadRealJpegImage();
        return uploadTestImage(filename, title, imageData);
    }

    private UUID uploadTestImage(String filename, String title, byte[] imageData) throws Exception {
        MockMultipartFile file = createImageFile(filename, imageData);
        ResultActions result = performImageUpload(file, title, "Description for " + title);
        Image image = extractImageFromResponse(result);
        return image.getId();
    }

    private void uploadMultipleTestImages(int count) throws Exception {
        for (int i = 1; i <= count; i++) {
            byte[] imageData = loadRealJpegImage();
            MockMultipartFile file = createImageFile("image-" + i + ".jpg", imageData);
            performImageUpload(file, "Test Image " + i, "Description " + i);
            Thread.sleep(10); // Ensure different timestamps
        }
    }

    /**
     * Loads a real JPEG image from test resources.
     * Uses the sample-001.jpg file (884KB) provided by the user.
     */
    private byte[] loadRealJpegImage() throws IOException {
        return getClass().getClassLoader()
                .getResourceAsStream("images/sample-001.jpg")
                .readAllBytes();
    }

    /**
     * Creates a MockMultipartFile from the real JPEG image.
     */
    private MockMultipartFile createRealImageFile() throws IOException {
        byte[] imageData = loadRealJpegImage();
        return new MockMultipartFile(
                "file",
                "sample-001.jpg",
                "image/jpeg",
                imageData
        );
    }
}
