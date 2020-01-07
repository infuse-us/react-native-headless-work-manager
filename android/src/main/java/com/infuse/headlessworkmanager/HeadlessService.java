package com.infuse.headlessworkmanager;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import javax.annotation.Nullable;

public class HeadlessService extends HeadlessJsTaskService {
    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();

        String taskKey = extras.getString(Constants.TASK_KEY);
        extras.remove(Constants.TASK_KEY);

        Integer timeout = extras.getInt(Constants.TIMEOUT);
        extras.remove(Constants.TIMEOUT);

        return new HeadlessJsTaskConfig(
                taskKey,
                Arguments.fromBundle(extras),
                timeout,
                true
        );
    }
}
