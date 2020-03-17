from osdu_api.model.search.sort_order import SortOrder

class SortQuery:
    def __init__(self, field: list, order: SortOrder):
        self.field = field
        self.order = order