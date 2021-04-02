package de.alewu.dsf.web;

public abstract class SecretsWebRequest extends AssetsWebRequest {

    public SecretsWebRequest(String url) {
        super("assets/" + url);
    }
}
