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
