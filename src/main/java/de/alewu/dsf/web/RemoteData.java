package de.alewu.dsf.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alewu.dsf.util.RuntimeContext;
import de.alewu.dsf.web.RemoteDataUpdateResult.Type;
import de.alewu.dsf.web.requests.AssetsVersionWebRequest;
import de.alewu.dsf.web.requests.GetIdentifiersWebRequest;
import de.alewu.dsf.web.requests.GetSecretsWebRequest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class RemoteData {

    private RemoteData() {
        // Util class
    }

    public static void load() {
        File file = new File("dsf", "disable_sync");
        if (file.exists()) {
            return;
        }
        RuntimeContext ctx = RuntimeContext.getInstance();
        JsonParser parser = new JsonParser();
        int currentVersion = -1;
        File localVersionFile = new File("dsf", "assets_version.json");
        if (!localVersionFile.exists()) {
            currentVersion = 0;
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(localVersionFile)))) {
                StringBuilder builder = new StringBuilder();
                while (reader.ready()) {
                    builder.append(reader.readLine());
                }
                JsonObject obj = (JsonObject) parser.parse(builder.toString());
                if (!obj.has("version")) {
                    ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.MISSING_MANDATORY_DATA, "Current Version [version]"));
                    return;
                }
                currentVersion = obj.get("version").getAsInt();
            } catch (Exception e) {
                ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Current Version"));
                e.printStackTrace();
            }
        }
        if (currentVersion == -1) {
            ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.MISSING_MANDATORY_DATA, "Current Version"));
            return;
        }

        int localVersion = currentVersion;
        new AssetsVersionWebRequest().send().whenCompleteAsync((str, t) -> {
            if (t != null) {
                ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Version"));
                t.printStackTrace();
                return;
            }
            try {
                JsonObject obj = (JsonObject) parser.parse(str);
                if (!obj.has("version")) {
                    ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.MISSING_MANDATORY_DATA, "Version [version]"));
                    return;
                }
                int remoteVersion = obj.get("version").getAsInt();
                if (localVersion >= remoteVersion) {
                    return;
                }

                downloadData(remoteVersion);
            } catch (JsonParseException e) {
                ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.NOT_PROPERLY_FORMATTED, "Version"));
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void downloadData(int version) {
        RuntimeContext ctx = RuntimeContext.getInstance();
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        File identifiersDirectory = new File("dsf/identifiers");
        if (!identifiersDirectory.exists()) {
            identifiersDirectory.mkdirs();
        }
        for (File file : Objects.requireNonNull(identifiersDirectory.listFiles())) {
            file.delete();
        }
        new GetIdentifiersWebRequest().send().whenCompleteAsync((iStr, iT) -> {
            try {
                JsonObject identifiersObj = (JsonObject) parser.parse(iStr);
                identifiersObj.entrySet().forEach(e -> {
                    File file = new File(identifiersDirectory, e.getKey() + ".json");
                    if (!file.exists()) {
                        try {
                            boolean created = file.createNewFile();
                            if (!created) {
                                return;
                            }
                        } catch (IOException ex) {
                            ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Identifiers"));
                            ex.printStackTrace();
                        }
                    }
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
                        gson.toJson(e.getValue(), writer);
                        writer.flush();
                    } catch (IOException ex) {
                        ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Identifiers"));
                        ex.printStackTrace();
                    }
                });

                File secretsDirectory = new File("dsf/secrets");
                if (!secretsDirectory.exists()) {
                    secretsDirectory.mkdirs();
                }
                for (File file : Objects.requireNonNull(secretsDirectory.listFiles())) {
                    file.delete();
                }
                new GetSecretsWebRequest().send().whenCompleteAsync((sStr, sT) -> {
                    try {
                        JsonObject secretsObj = (JsonObject) parser.parse(sStr);
                        secretsObj.entrySet().forEach(e -> {
                            File file = new File(secretsDirectory, e.getKey() + ".json");
                            if (!file.exists()) {
                                try {
                                    boolean created = file.createNewFile();
                                    if (!created) {
                                        return;
                                    }
                                } catch (IOException ex) {
                                    ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Secrets"));
                                    ex.printStackTrace();
                                }
                            }
                            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
                                gson.toJson(e.getValue(), writer);
                                writer.flush();
                            } catch (IOException ex) {
                                ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Secrets"));
                                ex.printStackTrace();
                            }
                        });

                        File localVersionFile = new File("dsf", "assets_version.json");
                        if (!localVersionFile.exists()) {
                            localVersionFile.createNewFile();
                        }
                        JsonObject versionObj = new JsonObject();
                        versionObj.addProperty("version", version);

                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localVersionFile)))) {
                            gson.toJson(versionObj, writer);
                            writer.flush();
                        } catch (IOException ex) {
                            ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Write New Version"));
                            ex.printStackTrace();
                        }
                    } catch (JsonParseException e) {
                        ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.NOT_PROPERLY_FORMATTED, "Secrets"));
                        e.printStackTrace();
                    } catch (IOException e) {
                        ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.EXCEPTION_OCCURRED, "Update Version"));
                        e.printStackTrace();
                    }
                });
            } catch (JsonParseException e) {
                ctx.setRemoteDataUpdateResult(new RemoteDataUpdateResult(Type.NOT_PROPERLY_FORMATTED, "Identifiers"));
                e.printStackTrace();
            }
        });
    }

}
