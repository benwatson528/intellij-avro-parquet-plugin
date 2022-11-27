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

  public AvroFileReader(File file) throws OutOfMemoryError, IOException {
    this.file = file;
    GenericDataConfigurer.configureGenericData();
    this.datumReader = new GenericDatumReader<>(null, null, GenericData.get());
    getRecords(1);
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
