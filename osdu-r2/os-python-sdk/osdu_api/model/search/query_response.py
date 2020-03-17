
class QueryResponse:
    def __init__(self, results: list, aggregations: list):
        self.results = results
        self.aggregations = aggregations