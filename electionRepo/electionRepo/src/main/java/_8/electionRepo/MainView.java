package _8.electionRepo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    private final ElectionRepository electionRepository;

    private final ComboBox<String> stateComboBox = new ComboBox<>("State");
    private final ComboBox<Integer> electionYearComboBox = new ComboBox<>("electionYear");
    private final ComboBox<String> countyComboBox = new ComboBox<>("County");
    private final ComboBox<String> electionTypeComboBox = new ComboBox<>("Election Type");
    private final Button searchButton = new Button("Search");
    private final Grid<Election> grid = new Grid<>(Election.class);

    @Autowired
    public MainView(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;

        setSpacing(true);

        // Configure search fields
        configureComboBoxes();

        // Configure grid
        grid.setColumns("state", "electionYear", "county", "electionType");
        grid.addComponentColumn(election -> {
            String boxDownloadUrl = generateBoxDownloadUrl(election.getElectionData());
            Anchor downloadLink = new Anchor(boxDownloadUrl, "Download CVR");
            downloadLink.setTarget("_blank");
            return downloadLink;
        }).setHeader("Download CVR");

        // Add components to layout
        add(stateComboBox, electionYearComboBox, countyComboBox, electionTypeComboBox, searchButton, grid);

        // Add search functionality
        searchButton.addClickListener(event -> searchElections());
    }

    private void configureComboBoxes() {
        // Populate ComboBoxes with options from the database
        List<String> states = electionRepository.findDistinctStates();
        stateComboBox.setItems(states);

        List<Integer> electionYears = electionRepository.findDistinctElectionYears();
        electionYearComboBox.setItems(electionYears);

        List<String> counties = electionRepository.findDistinctCounties();
        countyComboBox.setItems(counties);

        List<String> electionTypes = electionRepository.findDistinctElectionTypes();
        electionTypeComboBox.setItems(electionTypes);
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
        String boxApiUrl = "https://api.box.com/2.0/files/" + fileId + "/content";
        return boxApiUrl + "?access_token=" + getAccessToken();
    }

    // Helper method to get an access token (use your method to fetch the token securely)
    private String getAccessToken() {
        // Replace with your logic for fetching a valid access token
        return "LEZ4u7Fx82wCG1bJna2CudQJ4Meo12Ri";
    }

}


