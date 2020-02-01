package uk.co.hadoopathome.intellij.avro.fileformat;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
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

  /**
   * Source: https://stackoverflow.com/a/55476606/729819
   *
   * @param file the Parquet file to be read
   */
  public ParquetReader(File file) throws IOException {
    System.out.println("parquet inside");

    Configuration conf = new Configuration();
    //    conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
    //    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
    conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
    System.out.println("test");

    Path path = new Path(file.getPath());
    LOGGER.info("Path = " + path);
    System.out.println("path = " + path);

    //
    //    ParquetMetadata readFooter = ParquetFileReader.readFooter(conf, path,
    // ParquetMetadataConverter.NO_FILTER);
    //    MessageType schema = readFooter.getFileMetaData().getSchema();
    //    ParquetFileReader r = new ParquetFileReader(conf, path, readFooter);
    //
    //    PageReadStore pages = null;
    //    try {
    //      while (null != (pages = r.readNextRowGroup())) {
    //        final long rows = pages.getRowCount();
    //        System.out.println("Number of rows: " + rows);
    //
    //        final MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
    //        final RecordReader recordReader = columnIO.getRecordReader(pages, new
    // GroupRecordConverter(schema));
    //        for (int i = 0; i < rows; i++) {
    //          final Group g = (Group) recordReader.read();
    //          String result = convertParquetGroupToJsonString(g);
    //
    //          System.out.println("result = "+result);
    //        }
    //      }
    //    } finally {
    //      r.close();
    //    }

    // add the decimal conversion to a generic data model

    System.out.println("pathparquet = " + path);
    HadoopInputFile inputFile = HadoopInputFile.fromPath(path, conf);
    System.out.println("inputFile = " + inputFile);

    this.reader = ParquetFileReader.open(inputFile);
    System.out.println("reader = " + reader);

    this.schema = reader.getFileMetaData().getSchema();

    //    ParquetMetadata footer =
    //        ParquetFileReader.readFooter(conf, path, ParquetMetadataConverter.NO_FILTER);
    //    System.out.println("footer = "+footer);
    // this.schema = footer.getFileMetaData().getSchema();
    System.out.println("schema = " + schema);

    // this.reader = new ParquetFileReader(conf, path, footer);
    System.out.println("reader");
  }

  @Override
  public String getSchema() {
    return ""; // this.schema.toString();
  }

  @Override
  public List<String> getRecords(int numRecords) throws IOException {
    //    int i = 0;
    //    List<String> records = new ArrayList<>();
    //
    //    GenericRecord rec;
    //    try {
    //      while ((rec = reader.read()) != null && i < numRecords) {
    //        records.add(rec.toString());
    //        i++;
    //      }
    //    } catch (IllegalArgumentException e) {
    //      LOGGER.warn("Omitting Parquet row", e);
    //    }
    //
    //    return records;

    System.out.println("getRecords");
    int totalNumRecordsRead = 0;
    PageReadStore pages;
    List<String> records = new ArrayList<>();
    while (null != (pages = this.reader.readNextRowGroup())) {
      final long rows = pages.getRowCount();
      final MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(this.schema);
      final RecordReader<Group> recordReader =
          columnIO.getRecordReader(pages, new GroupRecordConverter(this.schema));
      for (int j = 0; j < rows; j++) {
        System.out.println("in group");
        final Group group = recordReader.read();
        records.add(convertParquetGroupToJsonString(group));
        totalNumRecordsRead++;
        if (totalNumRecordsRead >= numRecords) {
          LOGGER.info("Retrieved " + totalNumRecordsRead + " records");
          System.out.println("Retrieved " + totalNumRecordsRead + " records");
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
    System.out.println(jsonObject.toString());
    return jsonObject.toString();
  }
}
