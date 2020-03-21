package uk.co.hadoopathome.intellij.viewer.fileformat;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.generic.GenericData;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

public class ParquetFileReader implements Reader {

  private static final Logger LOGGER = Logger.getInstance(ParquetFileReader.class);
  private final Path path;

  public ParquetFileReader(File file) {
    this.path = file.toPath();
  }

  @Override
  public String getSchema() throws IOException {
    ParquetReader<Object> pReader =
        AvroParquetReader.builder(new LocalInputFile(this.path)).build();
    GenericData.Record firstRecord = (GenericData.Record) pReader.read();
    if (firstRecord == null) {
      throw new IOException("Can't process empty Parquet file");
    }
    return firstRecord.getSchema().toString(true);
  }

  @Override
  public List<String> getRecords(int numRecords) throws IOException, IllegalArgumentException {
    List<String> records = new ArrayList<>();
    try (ParquetReader<Object> pReader =
        AvroParquetReader.builder(new LocalInputFile(this.path)).build()) {
      GenericData.Record value;
      for (int i = 0; i < numRecords; i++) {
        value = (GenericData.Record) pReader.read();
        if (value == null) {
          LOGGER.info(String.format("Retrieved %d records", records.size()));
          return records;
        } else {
          records.add(value.toString());
        }
      }
    }
    LOGGER.info(String.format("Retrieved %d records", records.size()));
    return records;
  }
}
