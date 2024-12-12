package _8.electionRepo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

	@Value("${box.developer.token}")
    private String DEV_TOKEN;
	private final ElectionRepository electionRepository;
    private final BoxService boxService; 

    // Search 
    private final ComboBox<String> stateComboBox = new ComboBox<>("State");
    private final ComboBox<Integer> electionYearComboBox = new ComboBox<>("Year");
    private final ComboBox<String> countyComboBox = new ComboBox<>("County");
    private final ComboBox<String> electionTypeComboBox = new ComboBox<>("Election Type");
    private final Button searchButton = new Button("Search");
    private final Grid<Election> grid = new Grid<>(Election.class);

    // Upload 
    private final TextField uploadElectionYearField = new TextField("Year");
    private final TextField uploadStateField = new TextField("State");
    private final TextField uploadCountyField = new TextField("County");
    private final ComboBox<String> uploadElectionTypeComboBox = new ComboBox<>("Election Type");
    private final MemoryBuffer memoryBuffer = new MemoryBuffer();
    private final Upload upload = new Upload(memoryBuffer);
    private final TextField sourceUrlField = new TextField("Source URL");
    private final ComboBox<String> uploadFileTypeComboBox = new ComboBox<>("File Type");
    private final Button uploadButton = new Button("Upload File");

    @Autowired
    public MainView(ElectionRepository electionRepository, BoxService boxService) {
        this.electionRepository = electionRepository;
        this.boxService = boxService;

        setSpacing(true);

        Tab searchTab = new Tab("Search Elections");
        Tab uploadTab = new Tab("Upload Data");
        Tabs tabs = new Tabs(searchTab, uploadTab);

        VerticalLayout searchLayout = createSearchLayout();
        VerticalLayout uploadLayout = createUploadLayout();

        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == searchTab) {
                searchLayout.setVisible(true);
                uploadLayout.setVisible(false);
            } else {
                searchLayout.setVisible(false);
                uploadLayout.setVisible(true);
            }
        });


        add(tabs, searchLayout, uploadLayout);
        searchLayout.setVisible(true);
        uploadLayout.setVisible(false);

    }

    private VerticalLayout createSearchLayout() {
        VerticalLayout layout = new VerticalLayout();

        configureComboBoxes(stateComboBox, electionYearComboBox, countyComboBox, electionTypeComboBox);

        grid.setColumns("state", "electionYear", "county", "electionType");
        grid.addComponentColumn(election -> {
            Anchor link = new Anchor(election.getSourceUrl(), election.getSourceUrl());
            link.setTarget("_blank");
            link.getElement().getStyle().set("word-break", "break-all");
            return link;
        }).setHeader("Source URL").setAutoWidth(true);
        grid.addComponentColumn(election -> {
            String boxDownloadUrl = generateBoxDownloadUrl(election.getElectionData());
            Anchor downloadLink = new Anchor(boxDownloadUrl, "Download CVR");
            downloadLink.setTarget("_blank");
            return downloadLink;
        }).setHeader("Download CVR");

        searchButton.addClickListener(event -> searchElections());

        layout.add(stateComboBox, electionYearComboBox, countyComboBox, electionTypeComboBox, searchButton, grid);
        return layout;
    }

    private VerticalLayout createUploadLayout() {
        VerticalLayout layout = new VerticalLayout();

        uploadElectionTypeComboBox.setItems(electionRepository.findDistinctElectionTypes());
        uploadFileTypeComboBox.setItems("CVR", "Ballot Images");

        upload.setDropLabel(new Span("CVR/Ballot Images"));
        upload.setAcceptedFileTypes(".csv", ".xml", ".pdf", ".xlsx", ".json", ".zip");

        // upload button 
        uploadButton.addClickListener(event -> {
            if (validateUploadInputs()) {
                InputStream fileStream = memoryBuffer.getInputStream();

                try {
                    String fileName = uploadStateField.getValue() + "-" + uploadCountyField.getValue() + "-" + uploadElectionYearField.getValue()
                    + "-" + uploadElectionTypeComboBox.getValue() + "-" + ballotImageOrCVR(uploadFileTypeComboBox.getValue()) + "." + FilenameUtils.getExtension(memoryBuffer.getFileName());
                    String fileId = boxService.uploadFile(fileName, fileStream, uploadStateField.getValue());

                    // save file details in database
                    Election newElection = new Election();
                    newElection.setElectionData(fileId);
                    newElection.setSourceUrl(sourceUrlField.getValue());
                    try {
                        int year = Integer.parseInt(uploadElectionYearField.getValue());
                        newElection.setElectionYear(year);
                    } catch (NumberFormatException e) {
                        Notification.show("Please enter a valid year", 3000, Notification.Position.MIDDLE);
                    }
                    newElection.setState(uploadStateField.getValue());
                    newElection.setCounty(uploadCountyField.getValue());
                    newElection.setElectionType(uploadElectionTypeComboBox.getValue());

                    electionRepository.save(newElection);
                    
                    try (FileWriter fw = new FileWriter("src/main/resources/data.sql", true)) {
                        fw.write(String.format(
                            "INSERT INTO election (election_year, state, county, election_type, election_data, source_url) VALUES (%s, '%s', '%s', '%s', '%s', '%s');%n",
                            newElection.getElectionYear(), newElection.getState(), newElection.getCounty(), newElection.getElectionType(), newElection.getElectionData(), newElection.getSourceUrl()
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error appending to data.sql");
                    }

                    sourceUrlField.clear();
                    upload.getElement().setProperty("files", ""); 
                    Notification.show("File uploaded and saved successfully!");
                } catch (Exception e) {
                    Notification.show("Failed to upload file: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            } else {
                Notification.show("Please fill in all required fields.", 5000, Notification.Position.MIDDLE);
            }
        });

        layout.add(uploadElectionYearField, uploadStateField, uploadCountyField, uploadElectionTypeComboBox, sourceUrlField, uploadFileTypeComboBox, upload, uploadButton);
        return layout;
    }

    private String ballotImageOrCVR(String value) {
		if (value.equals("CVR")) {
			return "CVR";
		}
		else {
			return "IMG";
		}
	}

	private void configureComboBoxes(ComboBox<String> state, ComboBox<Integer> electionYear, ComboBox<String> county, ComboBox<String> electionType) {
        state.setItems(electionRepository.findDistinctStates());
        electionYear.setItems(electionRepository.findDistinctElectionYears());
        county.setItems(electionRepository.findDistinctCounties());
        electionType.setItems(electionRepository.findDistinctElectionTypes());
    }

    private boolean validateUploadInputs() {
        return uploadElectionYearField.getValue() != null &&
                uploadStateField.getValue() != null &&
                uploadCountyField.getValue() != null &&
                uploadElectionTypeComboBox.getValue() != null &&
                memoryBuffer.getFileName() != null &&
                !sourceUrlField.isEmpty() &&
                uploadFileTypeComboBox.getValue() != null;
    }

    private void searchElections() {
        String state = stateComboBox.getValue();
        Integer electionYear = electionYearComboBox.getValue();
        String county = countyComboBox.getValue();
        String electionType = electionTypeComboBox.getValue();

        List<Election> results = electionRepository.search(state, electionYear, county, electionType);
        grid.setItems(results);
    }

    private String generateBoxDownloadUrl(String fileId) {
        return "https://api.box.com/2.0/files/" + fileId + "/content?access_token=" + DEV_TOKEN;
    }
}
