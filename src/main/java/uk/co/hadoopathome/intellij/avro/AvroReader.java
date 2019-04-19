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
        return dataFileReader.getSchema().toString(true);
    }

    List<String> getRecords(int numRecords) {
        int i = 0;
        List<String> records = new ArrayList<>();
        while (this.dataFileReader.hasNext() && i < numRecords) {
            records.add(this.dataFileReader.next().toString());
            i++;
        }
        return records;
    }
}
//java.lang.LinkageError: loader constraint violation: when resolving method "org.slf4j.impl.StaticLoggerBinder.getLoggerFactory()Lorg/slf4j/ILoggerFactory;" the class loader (instance of com/intellij/ide/plugins/cl/PluginClassLoader) of the current class, org/slf4j/LoggerFactory, and the class loader (instance of com/intellij/util/lang/UrlClassLoader) for the method's defining class, org/slf4j/impl/StaticLoggerBinder, have different Class objects for the type org/slf4j/ILoggerFactory used in the signature
