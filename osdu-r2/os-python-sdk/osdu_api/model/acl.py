
'''
Acl model mirroring what's found in core common
'''
class Acl:
    def __init__(self, viewers: list, owners: list):
        self.viewers = viewers
        self.owners = owners
