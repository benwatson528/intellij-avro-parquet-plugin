package uk.co.hadoopathome.intellij.viewer.fileformat;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

public class AvroFileReader implements Reader {

  private static final Logger LOGGER = Logger.getInstance(AvroFileReader.class);
  private final DataFileReader<GenericRecord> dataFileReader;

  public AvroFileReader(File file) throws OutOfMemoryError, IOException {
    GenericData genericData = GenericData.get();
    genericData = addLogicalTypes(genericData);
    GenericDatumReader<GenericRecord> datumReader =
        new GenericDatumReader<>(null, null, genericData);
    this.dataFileReader = new DataFileReader<>(file, datumReader);
  }

  @Override
  public String getSchema() {
    return this.dataFileReader.getSchema().toString(true);
  }

  @Override
  public List<String> getRecords(int numRecords) {
    int i = 0;
    List<String> records = new ArrayList<>();
    while (this.dataFileReader.hasNext() && i < numRecords) {
      records.add(this.dataFileReader.next().toString());
      i++;
    }
    LOGGER.info(String.format("Retrieved %d records", i));
    return records;
  }

  private GenericData addLogicalTypes(GenericData genericData) {
    genericData.addLogicalTypeConversion(new Conversions.DecimalConversion());
    genericData.addLogicalTypeConversion(new Conversions.UUIDConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimeMillisConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
    genericData.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
    return genericData;
  }
}
