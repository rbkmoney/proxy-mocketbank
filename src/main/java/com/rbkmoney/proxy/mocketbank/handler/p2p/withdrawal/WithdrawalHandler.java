package com.rbkmoney.proxy.mocketbank.handler.p2p.withdrawal;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsIDStorageApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsDomainWrapper;
import com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals.WithdrawalsProviderAdapterWrapper;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
public class WithdrawalHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CdsIDStorageApi cdsIdStorageApi;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link WithdrawalHandler} instance with the given
     * initial parameters to be constructed.
     *
     * @param cdsIdStorageApi the field's cdsIdStorageApi (see {@link #cdsIdStorageApi}).
     */
    @Autowired
    public WithdrawalHandler(
            CdsIDStorageApi cdsIdStorageApi
    ) {
        this.cdsIdStorageApi = cdsIdStorageApi;
    }

    public ProcessResult handler(
            Withdrawal withdrawal,
            Value state,
            Map<String, String> options
    ) throws TException {
        String withdrawalId = withdrawal.getId();

        String identityDocumentToken = getIdentityDocumentToken(withdrawal);
        com.rbkmoney.identdocstore.identity_document_storage.IdentityDocument identityDocument = cdsIdStorageApi.get(identityDocumentToken);
        if (!identityDocument.isSetRussianDomesticPassport()) {
            throw new IllegalArgumentException("Not a passport");
        }

        return WithdrawalsProviderAdapterWrapper.makeProcessResult(
                WithdrawalsProviderAdapterWrapper.makeFinishIntentSuccess(
                        WithdrawalsDomainWrapper.makeTransactionInfo(
                                withdrawalId
                        )
                )
        );
    }

    private static String getIdentityDocumentToken(Withdrawal withdrawal) {

        Optional<List<com.rbkmoney.damsel.withdrawals.domain.IdentityDocument>> identityDocumentList =
                Optional.ofNullable(withdrawal.getSender())
                        .map(com.rbkmoney.damsel.withdrawals.domain.Identity::getDocuments);

        if (!identityDocumentList.isPresent()) {
            throw new IllegalArgumentException("Documents must be set");
        }

        return identityDocumentList.get()
                .stream().filter(
                        com.rbkmoney.damsel.withdrawals.domain.IdentityDocument::isSetRusDomesticPassport
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("RusDomesticPassport must be set"))
                .getRusDomesticPassport()
                .getToken();
    }

}