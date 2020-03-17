"""
A workflow creating a record
"""
from airflow import DAG
from airflow.operators.python_operator import PythonOperator
from airflow.utils.dates import days_ago
from datetime import datetime, timedelta
from create_records import create_records

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

workflow_name = 'create_records'
dag = DAG(workflow_name, default_args=default_args, schedule_interval=timedelta(days=1))

# comes from the experimental endpoint /api/experimental/dags/<DAG_NAME>/dag_runs  

create_records_op = PythonOperator(
    task_id='create_records',
    python_callable=create_records,
    provide_context=True,
    dag=dag
)
