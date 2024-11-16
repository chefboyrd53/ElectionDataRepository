package _8.electionRepo;


import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.vaadin.flow.data.provider.DataProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final BoxAPIConnection boxAPIConnection;

    @Autowired
    public ElectionService(ElectionRepository electionRepository, BoxAPIConnection boxAPIConnection) {
        this.electionRepository = electionRepository;
        this.boxAPIConnection = boxAPIConnection;
    }

    public void downloadFile(String boxFileId, OutputStream outputStream) {
        BoxFile file = new BoxFile(boxAPIConnection, boxFileId);
        file.download(outputStream);
    }

    public Election getElectionById(Long id) {
        return electionRepository.findById(id).orElseThrow(() -> new RuntimeException("Election not found"));
    }

	public List<Election> getAllElections() {
		return electionRepository.findAll();
	}
}
