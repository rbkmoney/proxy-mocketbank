package com.rbkmoney.proxy.test.utils.damsel;

import com.rbkmoney.damsel.base.Timer;
import com.rbkmoney.damsel.proxy.*;
import com.rbkmoney.damsel.user_interaction.UserInteraction;

public class ProxyWrapper {

    // FinishIntent
    public static Intent makeFinishIntentOk() {
        FinishIntent finishIntent = new FinishIntent();
        finishIntent.setStatus(ProxyWrapper.makeFinishStatusOk());
        Intent intent = new Intent();
        intent.setFinish(finishIntent);
        return intent;
    }

    public static Intent makeFinishIntentFailure(String code, String description) {
        FinishIntent finishIntent = new FinishIntent();
        finishIntent.setStatus(
                ProxyWrapper.makeFinishStatusFailure(
                        BaseWrapper.makeError(code, description)
                )
        );
        Intent intent = new Intent();
        intent.setFinish(finishIntent);
        return intent;
    }

    public static Intent makeIntentWithSleepIntent(Timer timer) {
        Intent intent = new Intent();
        intent.setSleep(ProxyWrapper.makeSleepIntent(timer));
        return intent;
    }

    public static SleepIntent makeSleepIntent(Timer timer) {
        SleepIntent sleepIntent = new SleepIntent();
        sleepIntent.setTimer(timer);
        return sleepIntent;
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

    public static FinishStatus makeFinishStatusFailure(com.rbkmoney.damsel.base.Error error) {
        return FinishStatus.failure(error);
    }

    public static FinishStatus makeFinishStatusOk() {
        return FinishStatus.ok(BaseWrapper.makeOk());
    }
}
