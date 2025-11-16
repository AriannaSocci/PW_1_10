package org.ancora_casini.PW_1_10.service;

import lombok.AllArgsConstructor;
import org.ancora_casini.PW_1_10.model.BaseData;
import org.ancora_casini.PW_1_10.model.EnvironmentalData;
import org.ancora_casini.PW_1_10.model.ProductionData;
import org.ancora_casini.PW_1_10.repository.EnvironmentalDataRepository;
import org.ancora_casini.PW_1_10.repository.ProductionDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashboardService {

    private final EnvironmentalDataRepository environmentalDataRepository;

    private final ProductionDataRepository productionDataRepository;

    public Map<String, Object> getDashboardData(OffsetDateTime start, OffsetDateTime end) {
        Map<String, Object> dashboardData = new HashMap<>();

        dashboardData.put("environmentalData", getEnvironmentalData(start, end));
        dashboardData.put("productionData", getProductionData(start, end));

        return dashboardData;
    }

    private Map<String, Object> getEnvironmentalData(OffsetDateTime start, OffsetDateTime end) {
        Map<String, Object> regionSummaries = new HashMap<>();

        List<EnvironmentalData> recentData = environmentalDataRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);

        if (!recentData.isEmpty()) {

            var groupedByRegion = recentData.stream()
                    .collect(Collectors.groupingBy(EnvironmentalData::getRegion, Collectors.toList()));

            for (var entry : groupedByRegion.entrySet()) {
                String region = entry.getKey().name();
                List<EnvironmentalData> regionData = entry.getValue();

                // Reduce data to max 100 records with aggregation
                var reducedData = reduce(regionData);
                regionSummaries.put(region, reducedData);
            }
        }

        return regionSummaries;
    }

    private Map<String, Map<String, Object>> getProductionData(OffsetDateTime start, OffsetDateTime end) {
        Map<String, Map<String, Object>> productionSummaries = new HashMap<>();

        List<ProductionData> recentData = productionDataRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);

        if (!recentData.isEmpty()) {

            var groupedByRegion = recentData.stream()
                    .collect(Collectors.groupingBy(ProductionData::getRegion, Collectors.toList()));

            for (var regionEntry : groupedByRegion.entrySet()) {
                String region = regionEntry.getKey().name();
                List<ProductionData> regionData = regionEntry.getValue();

                var groupedByCrop = regionData.stream()
                        .collect(Collectors.groupingBy(ProductionData::getCrop, Collectors.toList()));

                Map<String, Object> cropSummaries = new HashMap<>();

                for (var cropEntry : groupedByCrop.entrySet()) {
                    String crop = cropEntry.getKey().name();
                    List<ProductionData> cropData = cropEntry.getValue();

                    cropSummaries.put(crop, cropData);
                }

                productionSummaries.put(region, cropSummaries);
            }
        }

        return productionSummaries;
    }


    private List<? extends BaseData> reduce(List<? extends BaseData> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        int size = records.size();

        // If already less than or equal to max, return as is
        int MAX_RECORDS = 100;
        if (size <= MAX_RECORDS) {
            return records;
        }

        // Calculate bucket size - how many records to average together
        int bucketSize = (int) Math.ceil((double) size / MAX_RECORDS);
        List<BaseData> reduced = new ArrayList<>();
        var first = records.getFirst();
        // Process records in buckets
        for (int i = 0; i < size; i += bucketSize) {
            int end = Math.min(i + bucketSize, size);
            var bucket = records.subList(i, end);

            // Aggregate the bucket
            var aggregated = switch (first) {
                case EnvironmentalData _ -> reduceEnvironmentalData(bucket.stream()
                        .map(r -> (EnvironmentalData) r)
                        .collect(Collectors.toList()));
                case ProductionData _ -> reduceProductionData(bucket.stream()
                        .map(r -> (ProductionData) r)
                        .collect(Collectors.toList()));
                default -> throw new IllegalArgumentException("Unsupported data type for reduction: " + first.getClass());
            };

            long avgEpochSecond = (long) records.stream()
                    .map(BaseData::getTimestamp)
                    .mapToLong(OffsetDateTime::toEpochSecond)
                    .average().orElse(first.getTimestamp().toEpochSecond());

            OffsetDateTime avgTimestamp = OffsetDateTime.ofInstant(Instant.ofEpochSecond(avgEpochSecond), first.getTimestamp().getOffset());
            aggregated.setTimestamp(avgTimestamp);
            aggregated.setRegion(first.getRegion());
            reduced.add(aggregated);
        }
        return reduced;
    }

    private EnvironmentalData reduceEnvironmentalData(List<EnvironmentalData> records) {

            // Create aggregated record
            EnvironmentalData aggregated = new EnvironmentalData();

            // Average numeric fields
            double avgTemperature = records.stream()
                    .mapToDouble(EnvironmentalData::getTemperature)
                    .average()
                    .orElse(0.0);

            double avgHumidity = records.stream()
                    .mapToDouble(EnvironmentalData::getHumidity)
                    .average()
                    .orElse(0.0);

            double avgPrecipitation = records.stream()
                    .mapToDouble(EnvironmentalData::getPrecipitation)
                    .average()
                    .orElse(0.0);

            double avgSoilMoisture = records.stream()
                    .mapToDouble(EnvironmentalData::getSoilMoisture)
                    .average()
                    .orElse(0.0);

            double avgWindSpeed = records.stream()
                    .mapToDouble(EnvironmentalData::getWindSpeed)
                    .average()
                    .orElse(0.0);

            // Set averaged values
            aggregated.setTemperature(Math.round(avgTemperature * 100.0) / 100.0);
            aggregated.setHumidity(Math.round(avgHumidity * 100.0) / 100.0);
            aggregated.setPrecipitation(Math.round(avgPrecipitation * 100.0) / 100.0);
            aggregated.setSoilMoisture(Math.round(avgSoilMoisture * 100.0) / 100.0);
            aggregated.setWindSpeed(Math.round(avgWindSpeed * 100.0) / 100.0);

        return aggregated;
    }

    private ProductionData reduceProductionData(List<ProductionData> records) {
            // Create aggregated record
            ProductionData aggregated = new ProductionData();

            // Use first record's non-numeric fields as representative
            ProductionData first = records.getFirst();
            aggregated.setCrop(first.getCrop());

            // Average numeric fields
            double avgHarvestQuantity = records.stream()
                    .mapToDouble(ProductionData::getHarvestQuantity)
                    .average()
                    .orElse(0.0);

            double avgGrowthDays = records.stream()
                    .mapToInt(ProductionData::getGrowthDays)
                    .average()
                    .orElse(0.0);

            double avgYieldPerHectare = records.stream()
                    .mapToDouble(ProductionData::getYieldPerHectare)
                    .average()
                    .orElse(0.0);

            BigDecimal avgProductionCost = records.stream()
                    .map(ProductionData::getProductionCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(records.size()), RoundingMode.HALF_UP);

            BigDecimal avgMarketPrice = records.stream()
                    .map(ProductionData::getMarketPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(records.size()), RoundingMode.HALF_UP);

            BigDecimal avgRevenue = records.stream()
                    .map(ProductionData::getRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(records.size()), RoundingMode.HALF_UP);

            double avgEfficiency = records.stream()
                    .mapToDouble(ProductionData::getEfficiency)
                    .average()
                    .orElse(0.0);

            // Set averaged values
            aggregated.setHarvestQuantity(Math.round(avgHarvestQuantity * 100.0) / 100.0);
            aggregated.setGrowthDays((int) Math.round(avgGrowthDays));
            aggregated.setYieldPerHectare(Math.round(avgYieldPerHectare * 100.0) / 100.0);
            aggregated.setProductionCost(avgProductionCost.setScale(2, RoundingMode.HALF_UP));
            aggregated.setMarketPrice(avgMarketPrice.setScale(2, RoundingMode.HALF_UP));
            aggregated.setRevenue(avgRevenue.setScale(2, RoundingMode.HALF_UP));
            aggregated.setEfficiency(Math.round(avgEfficiency * 100.0) / 100.0);

            return aggregated;

    }

}
