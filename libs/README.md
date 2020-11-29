## INT96 Support

INT96 support has been removed by Parquet, and so we must build our own parquet-avro JAR to provide support. This must be done with every new release of Parquet.

1. Clone `https://github.com/apache/parquet-mr` and check out the relevant release tag.
2. Modify `AvroSchemaConverter` in the `parquet-avro` module, modifying the `convertINT96` method to:
   ```java
   public Schema convertINT96(PrimitiveTypeName primitiveTypeName) { return Schema.create(Schema.Type.BYTES); }
   ```
3. Build the entire `parquet-mr` project with `mvn clean install` (`parquet-avro` is built quite early on; there's no need to wait for the entire process to finish).
4. Move `parquet-avro/target/parquet-avro-x.y.z-SNAPSHOT.jar` jar into `intellij-avro-parquet-viewer/libs`, deleting the old version.
5. Delete the `shaded` directory from the jar.
6. If the jar name has changed, update the `compile files` version reference in `intellij-avro-parquet-viewer/build.gradle`.
