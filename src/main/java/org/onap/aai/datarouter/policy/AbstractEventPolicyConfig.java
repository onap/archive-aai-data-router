package org.onap.aai.datarouter.policy;

import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersions;

public class AbstractEventPolicyConfig {
    private String sourceDomain;
    private String searchBaseUrl;
    private String searchEndpoint;
    private String searchEndpointDocuments;
    private String searchCertName;
    private String searchKeystorePwd;
    private String searchKeystore;
    private SchemaVersions schemaVersions;
    private SchemaLocationsBean schemaLocationsBean;

    public String getSourceDomain() {
        return sourceDomain;
    }

    public void setSourceDomain(String sourceDomain) {
        this.sourceDomain = sourceDomain;
    }

    public String getSearchBaseUrl() {
        return searchBaseUrl;
    }

    public void setSearchBaseUrl(String searchBaseUrl) {
        this.searchBaseUrl = searchBaseUrl;
    }

    public String getSearchEndpoint() {
        return searchEndpoint;
    }

    public void setSearchEndpoint(String searchEndpoint) {
        this.searchEndpoint = searchEndpoint;
    }

    public String getSearchEndpointDocuments() {
        return searchEndpointDocuments;
    }

    public void setSearchEndpointDocuments(String searchEndpointDocuments) {
        this.searchEndpointDocuments = searchEndpointDocuments;
    }

    public String getSearchCertName() {
        return searchCertName;
    }

    public void setSearchCertName(String searchCertName) {
        this.searchCertName = searchCertName;
    }

    public String getSearchKeystore() {
        return searchKeystore;
    }

    public void setSearchKeystore(String searchKeystore) {
        this.searchKeystore = searchKeystore;
    }

    public String getSearchKeystorePwd() {
        return searchKeystorePwd;
    }

    public void setSearchKeystorePwd(String searchKeystorePwd) {
        this.searchKeystorePwd = searchKeystorePwd;
    }

    public SchemaVersions getSchemaVersions() {
        return schemaVersions;
    }

    public void setSchemaVersions(SchemaVersions schemaVersions) {
        this.schemaVersions = schemaVersions;
    }

    public SchemaLocationsBean getSchemaLocationsBean() {
        return schemaLocationsBean;
    }

    public void setSchemaLocationsBean(SchemaLocationsBean schemaLocationsBean) {
        this.schemaLocationsBean = schemaLocationsBean;
    }
}

