package pl.dolien.echohub.file;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.dolien.echohub.exception.DirectoryCreationException;
import pl.dolien.echohub.exception.FileSaveException;
import pl.dolien.echohub.file.system.FileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private static final String FILE_SUCCESSFULLY_SAVED_MSG = "File successfully saved to: {}";
    private static final String FILE_SAVE_EXCEPTION_MSG = "Error saving file to: ";
    private static final String DIR_CREATION_EXCEPTION_MSG = "Failed to create the target folder: ";
    private static final String USER_FOLDER_PREFIX = "users";
    private static final String FILE_SEPARATOR = separator;
    private final FileSystem fileSystem;
    @Value("${application.file.uploads.media-output-path}")
    private String fileUploadPath;

    public String saveFile(
            @NonNull MultipartFile sourceFile,
            @NonNull String userId
            ) {
        final String fileUploadSubPath = USER_FOLDER_PREFIX + FILE_SEPARATOR + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(
            @NonNull MultipartFile sourceFile,
            @NonNull String fileUploadSubPath
    ) {
        final String finalUploadPath = fileUploadPath + FILE_SEPARATOR + fileUploadSubPath;
        createDirectoryIfNotExists(finalUploadPath);

        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String targetFilePath = finalUploadPath + FILE_SEPARATOR + currentTimeMillis() + fileExtension;

        return saveFileToDisk(sourceFile, targetFilePath);
    }

    private void createDirectoryIfNotExists(String finalUploadPath) {
        File targetFolder = fileSystem.getFile(finalUploadPath);
        if (!fileSystem.exists(targetFolder)) {
            boolean folderCreated = fileSystem.mkdirs(targetFolder);
            if (!folderCreated)
                throw new DirectoryCreationException(DIR_CREATION_EXCEPTION_MSG + targetFolder);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty())
            return "";

        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex != -1) ? filename.substring(lastDotIndex).toLowerCase() : "";
    }

    private String saveFileToDisk(MultipartFile sourceFile, String targetFilePath) {
        Path targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info(FILE_SUCCESSFULLY_SAVED_MSG, targetFilePath);
            return targetFilePath;
        } catch (IOException e) {
            throw new FileSaveException(FILE_SAVE_EXCEPTION_MSG + targetFilePath, e);
        }
    }
}
