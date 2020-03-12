from osdu_api.model.search.by_bounding_box import ByBoundingBox
from osdu_api.model.search.by_distance import ByDistance
from osdu_api.model.search.by_geo_polygon import ByGeoPolygon

class SpatialFilter:
    def __init__(self, field: str, by_bounding_box: ByBoundingBox, by_distance: ByDistance, by_geo_polygon: ByGeoPolygon):
        self.field = field
        self.by_bounding_box = by_bounding_box
        self.by_distance = by_distance
        self.by_bounding_box = by_geo_polygon