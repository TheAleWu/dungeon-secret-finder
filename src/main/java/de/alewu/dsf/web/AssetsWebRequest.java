package de.alewu.dsf.web;

public abstract class AssetsWebRequest extends AbstractWebRequest {

    public AssetsWebRequest(String url) {
        super(WebConstants.URL + url);
    }
}
