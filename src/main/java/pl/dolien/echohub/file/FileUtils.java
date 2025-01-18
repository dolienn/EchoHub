package pl.dolien.echohub.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.micrometer.common.util.StringUtils.isBlank;

@Slf4j
public class FileUtils {

    private static final String FILE_NOT_FOUND_MSG = "No file found at: {}";

    private FileUtils() {}

    public static byte[] readFileFromLocation(String fileUrl) {
        if (isBlank(fileUrl)) {
            return new byte[0];
        }

        try {
            Path file = new File(fileUrl).toPath();
            return Files.readAllBytes(file);
        } catch (IOException e) {
            log.warn(FILE_NOT_FOUND_MSG, fileUrl);
        }
        return new byte[0];
    }
}
