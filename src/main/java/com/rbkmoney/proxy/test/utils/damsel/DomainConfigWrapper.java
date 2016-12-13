package com.rbkmoney.proxy.test.utils.damsel;


import com.rbkmoney.damsel.domain.DomainObject;
import com.rbkmoney.damsel.domain_config.*;

import java.util.List;
import java.util.Map;


public class DomainConfigWrapper {

    public static InsertOp makeInsertOp(DomainObject domainObject) {
        InsertOp insertOp = new InsertOp();
        insertOp.setObject(domainObject);
        return insertOp;
    }

    public static UpdateOp makeUpdateOp(DomainObject newObject, DomainObject oldObject) {
        UpdateOp updateOp = new UpdateOp();
        updateOp.setNewObject(newObject);
        updateOp.setOldObject(oldObject);
        return updateOp;
    }

    public static RemoveOp makeRemoveOp(DomainObject domainObject) {
        RemoveOp removeOp = new RemoveOp();
        removeOp.setObject(domainObject);
        return removeOp;
    }

    public static Operation makeOperationInsert(DomainObject domainObject) {
        return Operation.insert(DomainConfigWrapper.makeInsertOp(domainObject));
    }

    public static Operation makeOperationUpdate(DomainObject newObject, DomainObject oldObject) {
        return Operation.update(DomainConfigWrapper.makeUpdateOp(newObject, oldObject));
    }

    public static Operation makeOperationRemove(DomainObject domainObject) {
        return Operation.remove(DomainConfigWrapper.makeRemoveOp(domainObject));
    }

    public static com.rbkmoney.damsel.domain_config.Reference makeReferenceHead() {
        Reference reference = new Reference();
        reference.setHead(DomainConfigWrapper.makeHead());
        return reference;
    }

    public static com.rbkmoney.damsel.domain_config.Reference makeReferenceVersion(long version) {
        Reference reference = new Reference();
        reference.setVersion(version);
        return reference;
    }

    public static Head makeHead() {
        return new Head();
    }

    public static Commit makeCommit(List<Operation> operations) {
        Commit commit = new Commit();
        commit.setOps(operations);
        return commit;
    }

    public static Snapshot makeSnapshot(long version, Map<com.rbkmoney.damsel.domain.Reference, DomainObject> domain) {
        Snapshot snapshot = new Snapshot();
        snapshot.setVersion(version);
        snapshot.setDomain(domain);
        return snapshot;
    }

}
