package _8.electionRepo;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;

@Service
public class BoxService {
    private final BoxAPIConnection boxAPIConnection;

    public BoxService() {
        this.boxAPIConnection = new BoxAPIConnection("YOUR_ACCESS_TOKEN");
    }

    public String uploadFile(String fileName, InputStream fileStream) {
        BoxFolder rootFolder = BoxFolder.getRootFolder(boxAPIConnection);
        BoxFile.Info fileInfo = rootFolder.uploadFile(fileStream, fileName);
        return fileInfo.getID();
    }
}
