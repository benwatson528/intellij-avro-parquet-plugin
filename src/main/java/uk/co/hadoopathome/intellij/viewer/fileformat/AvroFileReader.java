package uk.co.hadoopathome.intellij.viewer.fileformat;

import com.google.common.collect.Iterators;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

public class AvroFileReader implements Reader {

  private static final Logger LOGGER = Logger.getInstance(AvroFileReader.class);
  private final File file;
  private final GenericDatumReader<GenericRecord> datumReader;

  public AvroFileReader(File file) throws OutOfMemoryError {
    this.file = file;
    GenericDataConfigurer.configureGenericData();
    this.datumReader = new GenericDatumReader<>(null, null, GenericData.get());
  }

  @Override
  public String getSchema() throws IOException {
    try (DataFileReader<GenericRecord> dataFileReader =
        new DataFileReader<>(this.file, this.datumReader)) {
      return dataFileReader.getSchema().toString(true);
    }
  }

  @Override
  public int getNumRecords() throws IOException {
    try (DataFileReader<GenericRecord> dataFileReader =
        new DataFileReader<>(this.file, this.datumReader)) {
      return Iterators.size(dataFileReader);
    }
  }

  @Override
  public List<String> getRecords(int numRecords) throws IOException {
    try (DataFileReader<GenericRecord> dataFileReader =
        new DataFileReader<>(this.file, this.datumReader)) {
      int i = 0;
      List<String> records = new ArrayList<>();
      while (dataFileReader.hasNext() && i < numRecords) {
        records.add(dataFileReader.next().toString());
        i++;
      }
      LOGGER.info(String.format("Retrieved %d records", i));
      return records;
    }
  }
}
