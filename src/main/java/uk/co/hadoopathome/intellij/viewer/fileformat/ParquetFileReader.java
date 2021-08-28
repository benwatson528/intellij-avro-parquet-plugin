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

import com.intellij.openapi.diagnostic.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.api.Binary;

public class ParquetFileReader implements Reader {

  public static final String INT_96_BYTE_REGEX =
      "\\[-?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+, -?\\d+\\]";
  public static final Pattern PATTERN = Pattern.compile(INT_96_BYTE_REGEX);
  private static final Logger LOGGER = Logger.getInstance(ParquetFileReader.class);
  private final Path path;
  private final Configuration conf;

  public ParquetFileReader(File file) {
    this.path = file.toPath();
    this.conf = new Configuration();
    this.conf.set("parquet.avro.readInt96AsFixed", "true");
    GenericDataConfigurer.configureGenericData();
  }

  @Override
  public String getSchema() throws IOException {
    try (ParquetReader<Object> parquetReader =
        AvroParquetReader.builder(new LocalInputFile(this.path)).withConf(this.conf).build()) {
      GenericData.Record firstRecord = (GenericData.Record) parquetReader.read();
      if (firstRecord == null) {
        throw new IOException("Can't process empty Parquet file");
      }
      return firstRecord.getSchema().toString(true);
    }
  }

  @Override
  public int getNumRecords() throws IOException {
    try (ParquetReader<Object> parquetReader =
        AvroParquetReader.builder(new LocalInputFile(this.path))
            .withDataModel(GenericData.get())
            .withConf(this.conf)
            .build()) {
      GenericData.Record value;
      int i = 0;
      while (true) {
        value = (GenericData.Record) parquetReader.read();
        if (value == null) {
          return i;
        } else {
          i++;
        }
      }
    }
  }

  @Override
  public List<String> getRecords(int numRecords) throws IOException, IllegalArgumentException {
    try (ParquetReader<Object> parquetReader =
        AvroParquetReader.builder(new LocalInputFile(this.path))
            .withDataModel(GenericData.get())
            .withConf(this.conf)
            .build()) {
      List<String> records = new ArrayList<>();
      GenericData.Record value;
      for (int i = 0; i < numRecords; i++) {
        value = (GenericData.Record) parquetReader.read();
        if (value == null) {
          LOGGER.info(String.format("Retrieved %d records", records.size()));
          return records;
        } else {
          String jsonRecord =
              deserialize(value.getSchema(), toByteArray(value.getSchema(), value)).toString();
          jsonRecord = convertInt96(jsonRecord);
          records.add(jsonRecord);
        }
      }
      LOGGER.info(String.format("Retrieved %d records", records.size()));
      return records;
    }
  }

  private String convertInt96(String jsonRecord) {
    Matcher matcher = PATTERN.matcher(jsonRecord);
    if (matcher.find()) {
      System.out.println("found: " + matcher.group(1));
      int startIdx = matcher.start();
      int endIdx = matcher.end();
      String extracted = jsonRecord.substring(matcher.start(), matcher.end());
      String removedBrackets = extracted.substring(1, extracted.length() - 1);
      String[] split = removedBrackets.split(", ");

      Binary binary = Binary.fromReusedByteArray(bytes);
      long timestampMillis = ParquetTimestampUtils.getTimestampMillis(binary);
      ZonedDateTime utc = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.of("UTC"));
      String formattedTimestamp = utc.toString();
      // replace
      String updatedRecord =
          jsonRecord.substring(0, startIdx) + formattedTimestamp + jsonRecord.substring(endIdx);
      return convertInt96(updatedRecord);
    }
    return jsonRecord;
  }

  // Loop through the string
  // If I find a match:
  //   go into a new method
  //   extract it (from first + last chars)
  //   replace it and return the string
  //   run the regex again on the new string
  //   when the matches stop, end

  /**
   * Correctly converts timestamp-milis LogicalType values to strings. Taken from
   * https://stackoverflow.com/a/52041154/729819.
   */
  private GenericRecord deserialize(Schema schema, byte[] data) throws IOException {
    GenericData.get().addLogicalTypeConversion(new TimestampMillisConversion());
    InputStream is = new ByteArrayInputStream(data);
    Decoder decoder = DecoderFactory.get().binaryDecoder(is, null);
    DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema, schema, GenericData.get());
    return reader.read(null, decoder);
  }

  private byte[] toByteArray(Schema schema, GenericRecord genericRecord) throws IOException {
    GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
    writer.getData().addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
    writer.write(genericRecord, encoder);
    encoder.flush();
    return baos.toByteArray();
  }

  public static class TimestampMillisConversion extends Conversion<String> {
    public TimestampMillisConversion() {}

    public Class<String> getConvertedType() {
      return String.class;
    }

    public String getLogicalTypeName() {
      return "timestamp-millis";
    }

    public String fromLong(Long millisFromEpoch, Schema schema, LogicalType type) {
      return Instant.ofEpochMilli(millisFromEpoch).toString();
    }

    public Long toLong(String timestamp, Schema schema, LogicalType type) {
      return new Long(timestamp);
    }
  }
}
