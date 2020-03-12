from osdu_api.model.acl import Acl
from osdu_api.model.legal import Legal
from osdu_api.model.legal_compliance import LegalCompliance
from osdu_api.model.record_ancestry import RecordAncestry

'''
A record model mirroring what's found in core common
'''
class Record:
    def __init__(self, id: str, version: int, kind: str, acl: Acl, legal: Legal, data: dict, ancestry: RecordAncestry,
            meta: dict):
        self.id = id
        self.version = version
        self.kind = kind
        self.acl = acl
        self.legal = legal
        self.data = data
        self.ancestry = ancestry
        self.meta = meta

    '''
    Overloaded constructor meant to throw KeyError if any record values are missing
    from the dict
    '''
    @classmethod
    def from_dict(cls, record_dict: dict):
        id = record_dict['id']
        version = record_dict['version']
        kind = record_dict['kind']
        acl = Acl(record_dict['acl']['viewers'], record_dict['acl']['owners'])
        legal = Legal(record_dict['legal']['legaltags'], record_dict['legal']['otherRelevantDataCountries'], 
            LegalCompliance[record_dict['legal']['status']])
        data = record_dict['data']
        meta = record_dict['meta']

        parents = []
        try:
            parents = record_dict['ancestry']['parents']
        except KeyError:
            # warn the user that ancestry wasn't found, not essential attribute
            print('Attribute "ancestry" is missing from dict being converted to record')

        ancestry = RecordAncestry(parents)

        return cls(id, version, kind, acl, legal, data, ancestry, meta)

    def convert_to_dict(self):
        record_converted = self.__dict__
        record_converted['acl'] = self.acl.__dict__
        record_converted['legal'] = self.legal.get_dict()
        record_converted['ancestry'] = self.ancestry.__dict__
        return record_converted
