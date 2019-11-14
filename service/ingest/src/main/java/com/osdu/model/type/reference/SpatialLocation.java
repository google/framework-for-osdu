package com.osdu.model.type.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SpatialLocation {

  @JsonProperty("SpatialLocationCoordinatesDate")
  LocalDateTime spatialLocationCoordinatesDate;

  @JsonProperty("QuantitativeAccuracyBandID")
  String quantitativeAccuracyBandID;

  @JsonProperty("QualitativeSpatialAccuracyTypeID")
  String qualitativeSpatialAccuracyTypeID;

  @JsonProperty("CoordinateQualityCheckPerformedBy")
  String coordinateQualityCheckPerformedBy;

  @JsonProperty("CoordinateQualityCheckDateTime")
  LocalDateTime coordinateQualityCheckDateTime;

  @JsonProperty("CoordinateQualityCheckRemark")
  String coordinateQualityCheckRemark;

  @JsonProperty("Coordinates")
  List<Coordinate> coordinates;

  @JsonProperty("SpatialParameterTypeID")
  String spatialParameterTypeID;

  @JsonProperty("SpatialGeometryTypeID")
  String spatialGeometryTypeID;

  @JsonProperty("VerticalCRSID")
  String verticalCrsID;

  @JsonProperty("HorizontalCRSID")
  String horizontalCrsID;

  @JsonProperty("Elevation")
  Double elevation;

  @JsonProperty("HeightAboveGroundLevel")
  Double heightAboveGroundLevel;

  @JsonProperty("HeightAboveGroundLevelUOMID")
  String heightAboveGroundLevelUomID;

}
