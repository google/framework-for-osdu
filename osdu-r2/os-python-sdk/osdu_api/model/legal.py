from osdu_api.model.legal_compliance import LegalCompliance

'''
Legal model mirroring what's found in core common
'''
class Legal:
    def __init__(self, legaltags: list, other_relevant_data_countries: list, status: LegalCompliance):
        self.legaltags = legaltags
        self.other_relevant_data_countries = other_relevant_data_countries
        self.status = status

    def get_dict(self):
        legal_dict = {}
        legal_dict['legaltags'] = self.legaltags
        legal_dict['otherRelevantDataCountries'] = self.other_relevant_data_countries
        legal_dict['status'] = str(self.status)
        return legal_dict