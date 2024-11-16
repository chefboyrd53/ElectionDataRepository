package _8.electionRepo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {
	@Query("SELECT DISTINCT e.state FROM Election e")
	List<String> findDistinctStates();

	@Query("SELECT DISTINCT e.electionYear FROM Election e")
	List<Integer> findDistinctElectionYears();

	@Query("SELECT DISTINCT e.county FROM Election e")
	List<String> findDistinctCounties();

	@Query("SELECT DISTINCT e.electionType FROM Election e")
	List<String> findDistinctElectionTypes();

	@Query("SELECT e FROM Election e WHERE " +
	       "(?1 IS NULL OR e.state = ?1) AND " +
	       "(?2 IS NULL OR e.electionYear = ?2) AND " +
	       "(?3 IS NULL OR e.county = ?3) AND " +
	       "(?4 IS NULL OR e.electionType = ?4)")
	List<Election> search(String state, Integer year, String county, String electionType);
	
	List<Election> findAll();

}
