#
# Copyright 2018 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

ARG SPARK_IMAGE=apache/spark:v3.4.0
FROM ${SPARK_IMAGE}

COPY target/scala-2.12/datamanipulator-assembly-0.0.1.jar /opt/spark/jars/datamanipulator-assembly-0.0.1.jar
COPY src/main/resources/timeusage/atussum.csv /opt/spark/work-dir/src/main/resources/timeusage/atussum.csv

ENTRYPOINT ["/opt/entrypoint.sh"]