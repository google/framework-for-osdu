package com.osdu.model.delfi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Objects;
import org.springframework.util.StringUtils;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelfiSearchObject {
    private String kind;
    private Integer limit;
    private String query;
    private List<String> returnedFields;
    private Sort sort;
    private Boolean queryAsOwner;
    private SpatialFilter spatialFilter;
    private Integer offset;

    public void addToQuery(String searchTerm){
        if (StringUtils.isEmpty(query)){
            query = searchTerm;
        } else {
            //TODO: Check that we match with Lucene syntax
            query = query + " AND " + searchTerm;
        }
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getReturnedFields() {
        return returnedFields;
    }

    public void setReturnedFields(List<String> returnedFields) {
        this.returnedFields = returnedFields;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Boolean getQueryAsOwner() {
        return queryAsOwner;
    }

    public void setQueryAsOwner(Boolean queryAsOwner) {
        this.queryAsOwner = queryAsOwner;
    }

    public SpatialFilter getSpatialFilter() {
        return spatialFilter;
    }

    public void setSpatialFilter(SpatialFilter spatialFilter) {
        this.spatialFilter = spatialFilter;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelfiSearchObject that = (DelfiSearchObject) o;
        return Objects.equal(kind, that.kind) &&
                Objects.equal(limit, that.limit) &&
                Objects.equal(query, that.query) &&
                Objects.equal(returnedFields, that.returnedFields) &&
                Objects.equal(sort, that.sort) &&
                Objects.equal(queryAsOwner, that.queryAsOwner) &&
                Objects.equal(spatialFilter, that.spatialFilter) &&
                Objects.equal(offset, that.offset);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(kind, limit, query, returnedFields, sort, queryAsOwner, spatialFilter, offset);
    }

    @Override
    public String toString() {
        return "DelfiSearchObject{" +
                "kind='" + kind + '\'' +
                ", limit=" + limit +
                ", query='" + query + '\'' +
                ", returnedFields=" + returnedFields +
                ", sort=" + sort +
                ", queryAsOwner=" + queryAsOwner +
                ", spatialFilter=" + spatialFilter +
                ", offset=" + offset +
                '}';
    }
}
