package _8.electionRepo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Election {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int electionYear;
    private String state;
    private String county;
    private String electionType;
    private String electionData;
    private String sourceUrl;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getElectionYear() { return electionYear; }
    public void setElectionYear(int electionYear) { this.electionYear = electionYear; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public String getElectionType() { return electionType; }
    public void setElectionType(String electionType) { this.electionType = electionType; }

    public String getElectionData() { return electionData; }
    public void setElectionData(String electionData) { this.electionData = electionData; }
    
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}

