package uk.co.hadoopathome.intellij.avro.fileformat;

import java.util.List;

public interface Reader {

  List<String> getRecords(int numRecords);

  String getSchema();
}
