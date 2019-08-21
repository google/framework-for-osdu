package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.common.base.Objects;

import java.util.List;

public class OSDUSearchObject {
    private Integer count;
    private Integer start;
    private String fulltext;
    @JsonAlias("metadata")
    private Object metadata;
    private List<String> facets;
    @JsonAlias("full-results")
    private Boolean fullResults;
    private Sort sort;
    @JsonAlias("geo-location")
    private GeoLocation geoLocation;

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getFulltext() {
        return fulltext;
    }

    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public List<String> getFacets() {
        return facets;
    }

    public void setFacets(List<String> facets) {
        this.facets = facets;
    }

    public Boolean getFullResults() {
        return fullResults;
    }

    public void setFullResults(Boolean fullResults) {
        this.fullResults = fullResults;
    }

    public Sort getSort() {
        return sort;
    }


    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OSDUSearchObject that = (OSDUSearchObject) o;
        return Objects.equal(count, that.count) &&
                Objects.equal(start, that.start) &&
                Objects.equal(fulltext, that.fulltext) &&
                Objects.equal(metadata, that.metadata) &&
                Objects.equal(facets, that.facets) &&
                Objects.equal(fullResults, that.fullResults) &&
                Objects.equal(sort, that.sort) &&
                Objects.equal(geoLocation, that.geoLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(count, start, fulltext, metadata, facets, fullResults, sort, geoLocation);
    }

    @Override
    public String toString() {
        return "OSDUSearchObject{" +
                "count=" + count +
                ", start=" + start +
                ", fulltext='" + fulltext + '\'' +
                ", metadata=" + metadata +
                ", facets=" + facets +
                ", fullResults=" + fullResults +
                ", sort=" + sort +
                ", geoLocation=" + geoLocation +
                '}';
    }
}
