package com.rbkmoney.proxy.mocketbank.utils.cds;


import com.rbkmoney.identdocstore.identity_document_storage.*;
import com.rbkmoney.identdocstore.identity_document_storage.IdentityDocumentStorageSrv;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Anatoly Cherkasov
 */
@Service
public class CdsIDStorageApi implements IdentityDocumentStorageSrv.Iface {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsStorageApi.class);

    private IdentityDocumentStorageSrv.Iface cdsIDStorageApi;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link CdsIDStorageApi} instance.
     */
    public CdsIDStorageApi() {
        // Constructs default a new {@link CdsIDStorageApi CdsIDStorageApi} instance.
    }

    /**
     * Constructs a new {@link CdsIDStorageApi} instance with the given
     * initial parameters to be constructed.
     *
     * @param cdsIDStorageApi the field's cdsIDStorageApi (see {@link #cdsIDStorageApi}).
     */
    @Autowired
    public CdsIDStorageApi(IdentityDocumentStorageSrv.Iface cdsIDStorageApi) {
        this.cdsIDStorageApi = cdsIDStorageApi;
    }


    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    @Override
    public String put(IdentityDocument identity_document) throws TException {
        LOGGER.info("putIdentityDocument: identity_document: {}", identity_document);
        try {
            String response = cdsIDStorageApi.put(identity_document);
            LOGGER.info("putIdentityDocument: response {}, identity_document: {}", response, identity_document);
            return response;
        } catch (Exception ex) {
            throw new CdsException(String.format("Exception in putIdentityDocument with identity_document: %s", identity_document), ex);
        }
    }

    @Override
    public IdentityDocument get(String token) throws IdentityDocumentNotFound, TException {
        LOGGER.info("getIdentityDocument: token: {}", token);
        try {
            IdentityDocument identityDocument = cdsIDStorageApi.get(token);
            LOGGER.info("getIdentityDocument: response, token: {}", token);
            return identityDocument;
        } catch (Exception ex) {
            throw new CdsException(String.format("Exception in getIdentityDocument with token: %s", token), ex);
        }
    }

}
