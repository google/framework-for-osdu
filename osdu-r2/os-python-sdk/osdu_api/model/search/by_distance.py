from osdu_api.model.search.coordinate import Coordinate

class ByDistance:
    def __init__(self, distance: float, coordinate: Coordinate):
        self.distance = distance
        self.coordinate = coordinate