package uk.co.hadoopathome.intellij.avro.fileformat;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

public class AvroReader implements Reader {
  private static final Logger LOGGER = Logger.getInstance(AvroReader.class);
  private DataFileReader<GenericRecord> dataFileReader;

  public AvroReader(File file) throws OutOfMemoryError, IOException {
    DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
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
}
