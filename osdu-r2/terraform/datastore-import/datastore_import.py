#   Copyright 2020 Google LLC
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

"""Imports JSON dumps into Cloud Datastore."""
import json
from google.cloud import datastore


DATASTORE_CLIENT = datastore.Client(namespace="odes-namespace")


def import_data(jsonfile):
    """Load JSON, parse it into Datastore Entity object."""
    with open(jsonfile, "r") as infile:
        result = json.load(infile)

    for item in result:
        entity = item["entity"]
        kind = entity["key"]["path"][0]["kind"]
        key = DATASTORE_CLIENT.key(kind)
        entry = datastore.Entity(key=key)
        props = entity["properties"]
        exclude_list = []
        for key in props.keys():
            if "stringValue" in props[key].keys():
                entry[key] = props[key]["stringValue"]
            else:
                entry[key] = None
            if "excludeFromIndexes" in props[key].keys():
                exclude_list.append(key)
        entry.exclude_from_indexes = exclude_list
        DATASTORE_CLIENT.put(entry)
    infile.close()


if __name__ == "__main__":
    import_data("ingestion-strategy.json")
    import_data("schema-data.json")
