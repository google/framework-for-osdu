#  Copyright 2020 Google LLC
#  Copyright 2020 Amazon
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

from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.utils.dates import days_ago
from datetime import datetime, timedelta
from create_records import create_records

"""
A workflow creating a record
"""

default_args = {
    'owner': 'Airflow',
    'depends_on_past': False,
    'start_date': days_ago(2),
    'email': ['airflow@example.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
    # 'queue': 'bash_queue',
    # 'pool': 'backfill',
    # 'priority_weight': 10,
    # 'end_date': datetime(2016, 1, 1),
}

workflow_name = 'Default_ingest'
dag = DAG(workflow_name, default_args=default_args, schedule_interval=timedelta(days=1))

# comes from the experimental endpoint /api/experimental/dags/<DAG_NAME>/dag_runs  

create_records_op = PythonOperator(
    task_id='create_records',
    python_callable=create_records,
    provide_context=True,
    dag=dag
)
