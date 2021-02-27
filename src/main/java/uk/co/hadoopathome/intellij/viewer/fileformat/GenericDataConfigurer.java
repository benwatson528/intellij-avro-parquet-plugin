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
