package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.base.Timer;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.user_interaction.UserInteraction;

import static com.rbkmoney.proxy.mocketbank.utils.damsel.BaseWrapper.makeTimerTimeout;

public class ProxyWrapper {

    // FinishIntent
    public static Intent makeFinishIntentSuccess() {
        return Intent.finish(new FinishIntent(ProxyWrapper.makeFinishStatusSuccess()));
    }

    public static Intent makeFinishIntentSuccessWithToken(String token) {
        return Intent.finish(new FinishIntent(ProxyWrapper.makeFinishStatusSuccess(token)));
    }

    public static Intent makeFinishIntentFailure(String code, String description) {
        return Intent.finish(new FinishIntent(ProxyWrapper.makeFinishStatusFailure(makeFailure(code, description))));
    }

    public static Intent makeFinishIntentFailure(Failure failure) {
        return Intent.finish(new FinishIntent(ProxyWrapper.makeFinishStatusFailure(failure)));
    }

    public static Intent makeIntentWithSuspendIntent(String tag, Integer timer, UserInteraction userInteraction) {
        return Intent.suspend(ProxyWrapper.makeSuspendIntent(tag, timer, userInteraction));
    }

    public static Intent makeIntentWithSuspendIntent(String tag, Integer timer) {
        return makeIntentWithSuspendIntent(tag, timer, null);
    }

    public static SuspendIntent makeSuspendIntent(String tag, Integer timer, UserInteraction userInteraction) {
        return new SuspendIntent(tag, makeTimerTimeout(timer)).setUserInteraction(userInteraction);
    }

    public static Intent makeIntentWithSleepIntent(Integer timer) {
        return Intent.sleep(ProxyWrapper.makeSleepIntent(makeTimerTimeout(timer)));
    }

    public static Intent makeIntentWithSleepIntent(Integer timer, UserInteraction userInteraction) {
        return Intent.sleep(ProxyWrapper.makeSleepIntent(timer, userInteraction));
    }

    public static SleepIntent makeSleepIntent(Timer timer) {
        return new SleepIntent(timer);
    }

    public static SleepIntent makeSleepIntent(Integer timer, UserInteraction userInteraction) {
        return makeSleepIntent(makeTimerTimeout(timer)).setUserInteraction(userInteraction);
    }

    public static FinishStatus makeFinishStatusFailure(Failure failure) {
        return FinishStatus.failure(failure);
    }

    public static FinishStatus makeFinishStatusSuccess() {
        return FinishStatus.success(new Success());
    }

    public static FinishStatus makeFinishStatusSuccess(String token) {
        return FinishStatus.success(new Success().setToken(token));
    }


    public static Failure makeFailure(String code, String description) {
        return new Failure(code).setReason(description);
    }

}
