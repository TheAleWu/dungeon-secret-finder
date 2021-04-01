package de.alewu.dsf.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;

public abstract class AbstractFile implements AutoCloseable {

    private File file;
    private JsonObject object;
    private boolean createdFile = false;

    public AbstractFile(String fileName) {
        this.file = new File("dsf", fileName);
        loadJsonObject();
    }

    public AbstractFile(String subDirectory, String fileName) {
        this.file = new File("dsf" + subDirectory, fileName);
        loadJsonObject();
    }

    public boolean exists() {
        return file.exists();
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public void createFileIfNotExists() {
        boolean created = file.getParentFile().mkdirs();
        if (!created && !file.getParentFile().exists()) {
            try {
                Files.createDirectory(file.getParentFile().toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!file.exists()) {
            try {
                Files.createFile(file.toPath());
                createdFile = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadJsonObject() {
        if (!file.exists()) {
            return;
        }
        if (createdFile) {
            createdFile = false;
            loadDefaults();
            save();
            loadJsonObject();
        } else {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
                Gson gson = new GsonBuilder().create();
                object = gson.fromJson(reader, JsonObject.class);
            } catch (Exception e) {
                if (!(e instanceof ClassCastException)) {
                    e.printStackTrace(); // object is JsonNull if file was newly created
                }
                this.object = new JsonObject();
                save();
            }
        }
    }

    public final void save() {
        createFileIfNotExists();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            gson.toJson(object, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public final JsonObject getObject() {
        if (object == null) {
            this.object = new JsonObject();
        }
        return object;
    }

    public final void delete() throws IOException {
        try {
            boolean delete = file.delete();
            if (!delete) {
                FileUtils.forceDelete(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            file.deleteOnExit();
        }
    }

    protected void loadDefaults() {
        // Overwrite if needed
    }

    @Override
    public final void close() {
        this.file = null;
    }
}
