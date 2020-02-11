package uk.co.hadoopathome.intellij.viewer.fileformat;

import java.io.IOException;
import java.util.List;

public interface Reader {

  List<String> getRecords(int numRecords) throws IOException;

  String getSchema() throws IOException;
}
