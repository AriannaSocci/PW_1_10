package org.ancora_casini.PW_1_10.repository;

import org.ancora_casini.PW_1_10.model.ProductionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ProductionDataRepository extends JpaRepository<ProductionData, String> {

    List<ProductionData> findByTimestampBetweenOrderByTimestampDesc(OffsetDateTime start, OffsetDateTime end);

}
