package pl.dolien.echohub.file.system;

import java.io.File;

public interface FileSystem {
    File getFile(String path);
    boolean exists(File file);
    boolean mkdirs(File file);
}
