package _8.electionRepo;

import java.io.InputStream;

import com.box.sdk.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoxService {
    private final BoxAPIConnection boxAPIConnection;

    @Autowired
    public BoxService(BoxAPIConnection boxAPIConnection) {
        this.boxAPIConnection = boxAPIConnection;
    }

    public String uploadFile(String fileName, InputStream fileStream, String state) {
        BoxFolder rootFolder = new BoxFolder(boxAPIConnection, "294217161504");

        BoxFolder stateFolder = rootFolder;
        boolean stateFound = false;

        for (BoxItem.Info itemInfo : rootFolder) {
            if (itemInfo instanceof BoxFolder.Info info) {
                if (info.getName().equals(state)) {
                    stateFound = true;
                    stateFolder = info.getResource();
                }
            }
        }
        if (!stateFound) {
            stateFolder = rootFolder.createFolder(state).getResource();
        }

        BoxFile.Info fileInfo = stateFolder.uploadFile(fileStream, fileName);
        return fileInfo.getID();
    }
}
