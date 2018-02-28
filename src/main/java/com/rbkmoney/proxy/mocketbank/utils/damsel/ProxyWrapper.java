package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.base.Timer;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.user_interaction.UserInteraction;

public class ProxyWrapper {

    // FinishIntent
    public static Intent makeFinishIntentSuccess() {
        FinishIntent finishIntent = new FinishIntent();
        finishIntent.setStatus(ProxyWrapper.makeFinishStatusSuccess());
        Intent intent = new Intent();
        intent.setFinish(finishIntent);
        return intent;
    }

    public static Intent makeFinishIntentFailure(String code, String description) {
        FinishIntent finishIntent = new FinishIntent();
        finishIntent.setStatus(
                ProxyWrapper.makeFinishStatusFailure(
                        makeFailure(code, description)
                )
        );
        Intent intent = new Intent();
        intent.setFinish(finishIntent);
        return intent;
    }

    public static Intent makeIntentWithSuspendIntent(String tag, Timer timer, UserInteraction userInteraction) {
        Intent intent = new Intent();
        intent.setSuspend(ProxyWrapper.makeSuspendIntent(tag, timer, userInteraction));
        return intent;
    }

    public static Intent makeIntentWithSuspendIntent(String tag, Timer timer) {
        return makeIntentWithSuspendIntent(tag, timer, null);
    }

    public static SuspendIntent makeSuspendIntent(String tag, Timer timer, UserInteraction userInteraction) {
        SuspendIntent suspendIntent = new SuspendIntent();
        suspendIntent.setTag(tag);
        suspendIntent.setTimeout(timer);
        suspendIntent.setUserInteraction(userInteraction);
        return suspendIntent;
    }

    public static FinishStatus makeFinishStatusFailure(Failure failure) {
        return FinishStatus.failure(failure);
    }

    public static FinishStatus makeFinishStatusSuccess() {
        return FinishStatus.success(new Success());
    }

    public static Failure makeFailure(String code, String description) {
        Failure failure = new Failure();
        failure.setCode(code);
        failure.setReason(description);
        return failure;
    }

}
