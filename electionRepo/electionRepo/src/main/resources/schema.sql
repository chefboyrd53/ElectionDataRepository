CREATE TABLE IF NOT EXISTS election (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    election_year INT NOT NULL,
    state VARCHAR(255) NOT NULL,
    county VARCHAR(255) NOT NULL,
    election_type VARCHAR(255) NOT NULL,
    election_data VARCHAR(255),
    source_url VARCHAR(1024)
);
