package uk.co.hadoopathome.intellij.viewer.fileformat;

import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;

public class GenericDataCreator {

  /**
   * Instantiates the {@link GenericData} object with {@link org.apache.avro.LogicalType}
   * conversions added.
   *
   * @return the populated {@link GenericData} instance.
   */
  static GenericData createGenericData() {
    GenericData genericData = GenericData.get();
    genericData.addLogicalTypeConversion(new Conversions.DecimalConversion());
    genericData.addLogicalTypeConversion(new Conversions.UUIDConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
    return genericData;
  }
}
