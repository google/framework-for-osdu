#  Copyright 2020 Amazon
#  Copyright 2020 Google LLC
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import os
import boto3

#TODO: look at using secrets manager to hold cognito credentials
'''
Reaches out to aws cognito for a valid user's token based on environment variables. 
The same pattern is used in OSDU java microservices' integration tests
'''
def get_bearer_token():
    ACCESS_KEY = os.environ.get('AWS_ACCESS_KEY_ID')
    SECRET_KEY = os.environ.get('AWS_SECRET_ACCESS_KEY')
    CLIENT_ID = os.environ.get('AWS_COGNITO_CLIENT_ID')
    USER = os.environ.get('AWS_COGNITO_AUTH_PARAMS_USER')
    PWD = os.environ.get('AWS_COGNITO_AUTH_PARAMS_PASSWORD')

    client = boto3.client(
        'cognito-idp',
        aws_access_key_id=ACCESS_KEY,
        aws_secret_access_key=SECRET_KEY
    )

    response = client.initiate_auth(
        AuthFlow='USER_PASSWORD_AUTH',
        ClientId=CLIENT_ID,
        AuthParameters={
            'USERNAME': USER,
            'PASSWORD': PWD
            }
    )

    return f'Bearer %s' % response['AuthenticationResult']['AccessToken']
