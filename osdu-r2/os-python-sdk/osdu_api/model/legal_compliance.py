from enum import Enum

'''
LegalCompliance model mirroring what's found in core common
'''
class LegalCompliance(Enum):
    incompliant = 0
    compliant = 1

    def __str__(self):
        return self.name
