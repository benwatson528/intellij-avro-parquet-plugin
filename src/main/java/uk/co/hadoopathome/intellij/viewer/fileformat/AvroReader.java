package uk.co.hadoopathome.intellij.viewer.fileformat;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
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

  public Schema getSchema2() {
    return this.dataFileReader.getSchema();
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

  public List<GenericData.Record> getRecordsRecord(int numRecords) {
    int i = 0;
    List<GenericData.Record> records = new ArrayList<>();
    while (this.dataFileReader.hasNext() && i < numRecords) {
      records.add((GenericData.Record) this.dataFileReader.next());
      i++;
    }
    LOGGER.info(String.format("Retrieved %d records", i));
    return records;
  }
}
