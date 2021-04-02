package de.alewu.dsf.web;

public abstract class IdentifiersWebRequest extends AssetsWebRequest {

    public IdentifiersWebRequest(String url) {
        super("identifiers/" + url);
    }
}
