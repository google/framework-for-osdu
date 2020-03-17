from osdu_api.model.search.coordinate import Coordinate

class ByBoundingBox:
    def __init__(self, top_left: Coordinate, bottom_right: Coordinate):
        self.top_left = top_left
        self.bottom_right = bottom_right