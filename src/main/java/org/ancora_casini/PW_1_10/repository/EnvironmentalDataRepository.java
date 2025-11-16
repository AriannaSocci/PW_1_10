package org.ancora_casini.PW_1_10.repository;

import org.ancora_casini.PW_1_10.model.EnvironmentalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EnvironmentalDataRepository extends JpaRepository<EnvironmentalData, String> {

    List<EnvironmentalData> findByTimestampBetweenOrderByTimestampDesc(OffsetDateTime start, OffsetDateTime end);

}
