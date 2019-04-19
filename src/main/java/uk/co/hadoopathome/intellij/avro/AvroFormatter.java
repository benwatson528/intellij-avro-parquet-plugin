package uk.co.hadoopathome.intellij.avro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class AvroFormatter {
    private static final Logger LOGGER = Logger.getInstance(AvroFormatter.class);

    String format(File file) {
        //Make sure it's avro or avsc
        byte[] rawText;
        try {
            rawText = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOGGER.error("Unable to read the file at " + file.getPath(), e);
            return null;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(new String(rawText));
        return gson.toJson(je);
    }
}
