# jqpdf

Publish libraries to Maven repository

```bash
./gradlew clean publish --info
```

Run application

```bash
./gradlew -p jqpdf-jni run --info --args="$(pwd)/test1.pdf"
```

```commandline
gradlew -p jqpdf-jni run --info --args="%cd%\test1.pdf"
```
