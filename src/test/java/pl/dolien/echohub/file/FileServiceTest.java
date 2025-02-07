package pl.dolien.echohub.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.springframework.web.multipart.MultipartFile;
import pl.dolien.echohub.exception.DirectoryCreationException;
import pl.dolien.echohub.exception.FileSaveException;
import pl.dolien.echohub.file.system.DefaultFileSystem;
import pl.dolien.echohub.file.system.FileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class FileServiceTest {

    private static final String USER_ID = "testUserId";
    private static final String FILE_NAME = "testing.txt";
    private static final String FILE_CONTENT = "Test content";
    private static final String USER_FOLDER_PREFIX = "users";
    private static final String FILE_SEPARATOR = separator;
    private static final String FILE_SAVE_EXCEPTION_MSG = "Error saving file to: ";
    private static final String DIR_CREATION_EXCEPTION_MSG = "Failed to create the target folder: " + null;
    @InjectMocks
    private FileService fileService;
    @TempDir
    Path tempDir;
    private String targetFilePathPrefix;

    @BeforeEach
    void setUp() {
        String testFileUploadPath = tempDir.toString();
        fileService = new FileService(new DefaultFileSystem());
        setField(fileService, "fileUploadPath", testFileUploadPath);
        targetFilePathPrefix =
                testFileUploadPath
                        + FILE_SEPARATOR
                        + USER_FOLDER_PREFIX
                        + FILE_SEPARATOR
                        + USER_ID
                        + FILE_SEPARATOR;
    }

    @Test
    void shouldSaveFile() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        byte[] fileContent = FILE_CONTENT.getBytes();
        mockSaveFile(multipartFile, fileContent);

        String result = fileService.saveFile(multipartFile, USER_ID);

        assertSaveFile(result);
        verifySaveFile(multipartFile);
    }

    @Test
    void shouldThrowExceptionWhenSavingFile() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        mockFileSaveException(multipartFile);

        FileSaveException exception = assertThrows(
                FileSaveException.class,
                () -> fileService.saveFile(multipartFile, USER_ID)
        );

        assertTrue(exception.getMessage().startsWith(FILE_SAVE_EXCEPTION_MSG + targetFilePathPrefix));
        verifySaveFile(multipartFile);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDirectory() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        FileSystem fileSystem = mock(FileSystem.class);
        FileService fileService = new FileService(fileSystem);

        DirectoryCreationException exception = assertThrows(DirectoryCreationException.class, () -> fileService.saveFile(multipartFile, USER_ID));

        assertEquals(DIR_CREATION_EXCEPTION_MSG, exception.getMessage());
    }

    private void mockSaveFile(
            MultipartFile multipartFile,
            byte[] fileContent
    ) throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getBytes()).thenReturn(fileContent);
    }

    private void assertSaveFile(String result) throws IOException {
        assertTrue(result.startsWith(targetFilePathPrefix));
        Path path = Paths.get(result);
        assertTrue(Files.exists(path)); // Verify the file is created
        assertEquals(FILE_CONTENT, new String(Files.readAllBytes(path)));
    }

    private void verifySaveFile(MultipartFile multipartFile) throws IOException {
        verify(multipartFile, times(1)).getOriginalFilename();
        verify(multipartFile, times(1)).getBytes();
    }

    private void mockFileSaveException(MultipartFile multipartFile) throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getBytes()).thenThrow(new IOException("Mock IOException"));
    }
}