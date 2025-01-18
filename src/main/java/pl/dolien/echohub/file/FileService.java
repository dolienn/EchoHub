package pl.dolien.echohub.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    public String saveFile(MultipartFile file, String senderId) {
        // todo save file to database and return file path
        return null;
    }
}
