package pl.dolien.echohub.file.system;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DefaultFileSystem implements FileSystem {
    @Override
    public File getFile(String path) {
        return new File(path);
    }

    @Override
    public boolean exists(File file) {
        return file.exists();
    }

    @Override
    public boolean mkdirs(File file) {
        return file.mkdirs();
    }
}
