/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.hadoopathome.intellij.viewer.fileformat;

import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;

public class GenericDataConfigurer {

  /**
   * Configures the singleton {@link GenericData} object with {@link org.apache.avro.LogicalType}
   * conversions added.
   *
   * @return the populated {@link GenericData} instance.
   */
  static void configureGenericData() {
    GenericData.get().addLogicalTypeConversion(new Conversions.DecimalConversion());
    GenericData.get().addLogicalTypeConversion(new Conversions.UUIDConversion());
    GenericData.get().addLogicalTypeConversion(new TimeConversions.TimeMicrosConversion());
    GenericData.get().addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
    GenericData.get().addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
    GenericData.get().addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
  }
}
