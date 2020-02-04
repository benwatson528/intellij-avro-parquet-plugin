package uk.co.hadoopathome.intellij.viewer.fileformat;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ParquetReader implements Reader {
  private static final Logger LOGGER = Logger.getInstance(ParquetReader.class);

  private final ParquetFileReader reader;
  private final MessageType schema;

  public ParquetReader(File file) throws IOException {
    List<SimpleGroup> simpleGroups = new ArrayList<>();

    this.reader = ParquetFileReader.open(MyInputFile.nioPathToInputFile(file.toPath()));

    this.schema = this.reader.getFileMetaData().getSchema();
  }

  @Override
  public String getSchema() {
    return this.schema.toString();
  }

  @Override
  public List<String> getRecords(int numRecords) throws IOException {

    int totalNumRecordsRead = 0;
    PageReadStore pages;
    List<String> records = new ArrayList<>();
    while (null != (pages = this.reader.readNextRowGroup())) {
      final long rows = pages.getRowCount();
      final MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(this.schema);
      final RecordReader<Group> recordReader =
          columnIO.getRecordReader(pages, new GroupRecordConverter(this.schema));
      for (int j = 0; j < rows; j++) {
        final Group group = recordReader.read();
        records.add(convertParquetGroupToJsonString(group));
        totalNumRecordsRead++;
        if (totalNumRecordsRead >= numRecords) {
          LOGGER.info("Retrieved " + totalNumRecordsRead + " records");
          return records;
        }
      }
    }

    LOGGER.info("Retrieved " + totalNumRecordsRead + " records");
    return records;
  }

  /**
   * Source: https://stackoverflow.com/a/55476606/729819
   *
   * @param group the Parquet Group (analogous to a row in any other file)
   * @return the JSONObject representing the record
   */
  private String convertParquetGroupToJsonString(final Group group) {
    JSONObject jsonObject = new JSONObject();

    int fieldCount = group.getType().getFieldCount();
    for (int field = 0; field < fieldCount; field++) {
      int valueCount = group.getFieldRepetitionCount(field);
      Type fieldType = group.getType().getType(field);
      String fieldName = fieldType.getName();
      for (int index = 0; index < valueCount; index++) {
        if (fieldType.isPrimitive()) {
          try {
            jsonObject.put(fieldName, group.getValueToString(field, index));
          } catch (JSONException e) {
            LOGGER.error("Unable to convert object to Parquet");
          }
        } else {
          try {
            jsonObject.put(
                fieldName, convertParquetGroupToJsonString(group.getGroup(field, index)));
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }
    }

    return jsonObject.toString();
  }
}