package _8.electionRepo;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Service
public class FileUploader {

    private final BoxAPIConnection boxAPIConnection;

    public FileUploader(BoxAPIConnection boxAPIConnection) {
        this.boxAPIConnection = boxAPIConnection;
    }

    public String uploadFile(String folderId, File file) {
        BoxFolder folder = new BoxFolder(boxAPIConnection, folderId);
        BoxFile.Info info = folder.uploadFile((InputStream) file.toPath(), file.getName());
        return info.getID();
    }
}

