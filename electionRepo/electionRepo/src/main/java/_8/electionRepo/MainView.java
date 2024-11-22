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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

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
    private final ComboBox<Integer> uploadElectionYearComboBox = new ComboBox<>("Year");
    private final ComboBox<String> uploadStateComboBox = new ComboBox<>("State");
    private final ComboBox<String> uploadCountyComboBox = new ComboBox<>("County");
    private final ComboBox<String> uploadElectionTypeComboBox = new ComboBox<>("Election Type");
    private final MemoryBuffer memoryBuffer = new MemoryBuffer();
    private final Upload upload = new Upload(memoryBuffer);
    private final TextField sourceUrlField = new TextField("Source URL");
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

        configureComboBoxes(uploadStateComboBox, uploadElectionYearComboBox, uploadCountyComboBox, uploadElectionTypeComboBox);

        upload.setDropLabel(new Span("CVR/Ballot Images"));
        upload.setAcceptedFileTypes(".csv", ".xml", ".pdf", ".xlsx", ".json");

        // upload button 
        uploadButton.addClickListener(event -> {
            if (validateUploadInputs()) {
                String fileName = memoryBuffer.getFileName();
                InputStream fileStream = memoryBuffer.getInputStream();

                try {
                    String fileId = boxService.uploadFile(fileName, fileStream);

                    // save file details in database
                    Election newElection = new Election();
                    newElection.setElectionData(fileId);
                    newElection.setSourceUrl(sourceUrlField.getValue());
                    newElection.setElectionYear(uploadElectionYearComboBox.getValue());
                    newElection.setState(uploadStateComboBox.getValue());
                    newElection.setCounty(uploadCountyComboBox.getValue());
                    newElection.setElectionType(uploadElectionTypeComboBox.getValue());

                    electionRepository.save(newElection);

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

        layout.add(uploadElectionYearComboBox, uploadStateComboBox, uploadCountyComboBox, uploadElectionTypeComboBox, sourceUrlField, upload, uploadButton);
        return layout;
    }

    private void configureComboBoxes(ComboBox<String> state, ComboBox<Integer> electionYear, ComboBox<String> county, ComboBox<String> electionType) {
        state.setItems(electionRepository.findDistinctStates());
        electionYear.setItems(electionRepository.findDistinctElectionYears());
        county.setItems(electionRepository.findDistinctCounties());
        electionType.setItems(electionRepository.findDistinctElectionTypes());
    }

    private boolean validateUploadInputs() {
        return uploadElectionYearComboBox.getValue() != null &&
                uploadStateComboBox.getValue() != null &&
                uploadCountyComboBox.getValue() != null &&
                uploadElectionTypeComboBox.getValue() != null &&
                memoryBuffer.getFileName() != null &&
                !sourceUrlField.isEmpty();
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
        return "https://api.box.com/2.0/files/" + fileId + "/content";
    }
}
