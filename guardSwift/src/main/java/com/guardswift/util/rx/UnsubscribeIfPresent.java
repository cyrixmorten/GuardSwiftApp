package com.guardswift.util.rx;

import io.reactivex.disposables.Disposable;

public final class UnsubscribeIfPresent {
    private UnsubscribeIfPresent() {//no instance
    }

    public static void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
