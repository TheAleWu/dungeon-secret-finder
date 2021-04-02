package de.alewu.dsf.web.requests;

import de.alewu.dsf.web.AssetsWebRequest;

public class GetSecretsWebRequest extends AssetsWebRequest {

    public GetSecretsWebRequest() {
        super("secrets.json");
    }
}
