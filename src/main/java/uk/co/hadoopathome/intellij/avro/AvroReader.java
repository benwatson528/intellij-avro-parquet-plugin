package uk.co.hadoopathome.intellij.avro;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AvroReader {
    private static final Logger LOGGER = Logger.getInstance(AvroReader.class);
    private final DataFileReader<GenericRecord> dataFileReader;

    AvroReader(File file) throws IOException {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
        this.dataFileReader = new DataFileReader<>(file, datumReader);
    }

    String getSchema() {
        return this.dataFileReader.getSchema().toString(true);
    }

    List<String> getRecords(int numRecords) {
        int i = 0;
        List<String> records = new ArrayList<>();
        while (this.dataFileReader.hasNext() && i < numRecords) {
            records.add(this.dataFileReader.next().toString());
            i++;
        }
        LOGGER.info("Retrieved " + i + " records");
        return records;
    }
}
