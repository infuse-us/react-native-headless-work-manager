package com.infuse.headlessworkmanager;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.infuse.headlessworkmanager.exceptions.MissingParameterException;
import com.infuse.headlessworkmanager.exceptions.UnknownExistingWorkPolicyException;
import com.infuse.headlessworkmanager.exceptions.UnknownTimeUnitException;
import com.infuse.headlessworkmanager.exceptions.UnknownWorkRequestTypeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class HeadlessWorkManagerModule extends ReactContextBaseJavaModule {
    private final WorkManager workManager;

    public HeadlessWorkManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Context context = reactContext.getApplicationContext();
        workManager = WorkManager.getInstance(context);
    }

    @Override
    public String getName() {
        return "HeadlessWorkManager";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        constants.put(Constants.ONE_TIME_WORK_REQUEST, Constants.ONE_TIME_WORK_REQUEST);
        constants.put(Constants.PERIODIC_WORK_REQUEST, Constants.PERIODIC_WORK_REQUEST);

        constants.put(Constants.KEEP, Constants.KEEP);
        constants.put(Constants.REPLACE, Constants.REPLACE);

        constants.put(Constants.MILLISECONDS, Constants.MILLISECONDS);
        constants.put(Constants.SECONDS, Constants.SECONDS);
        constants.put(Constants.MINUTES, Constants.MINUTES);
        constants.put(Constants.HOURS, Constants.HOURS);
        constants.put(Constants.DAYS, Constants.DAYS);

        return constants;
    }

    @ReactMethod
    public void enqueue(ReadableMap params, final Promise promise) {
        UUID workerId;

        try {
            checkKey(params, Constants.WORK_REQUEST_TYPE);
            String workRequestType = params.getString(Constants.WORK_REQUEST_TYPE);

            checkKey(params, Constants.TASK_KEY);
            String taskKey = params.getString(Constants.TASK_KEY);

            Boolean isUnique = params.hasKey(Constants.IS_UNIQUE) && params.getBoolean(Constants.IS_UNIQUE);

            switch (workRequestType) {
                case Constants.ONE_TIME_WORK_REQUEST:
                    workerId = enqueueOneTimeWork(taskKey, params, isUnique);
                    break;
                case Constants.PERIODIC_WORK_REQUEST:
                    workerId = enqueuePeriodicWork(taskKey, params, isUnique);
                    break;
                default:
                    throw new UnknownWorkRequestTypeException(workRequestType);
            }
        } catch (Exception e) {
            promise.reject(e);
            return;
        }

        WritableMap result = Arguments.createMap();
        result.putString(Constants.WORKER_ID, workerId.toString());
        promise.resolve(result);
    }

    @ReactMethod
    public void getWorkInfoById(String uuid, final Promise promise) {
        UUID workerId = UUID.fromString(uuid);
        ListenableFuture<WorkInfo> future = workManager.getWorkInfoById(workerId);
        resolveFutureWorkInfo(future, promise);
    }

    @ReactMethod
    public void getWorkInfosForUniqueWork(String taskName, final Promise promise) {
        ListenableFuture<List<WorkInfo>> future = workManager.getWorkInfosForUniqueWork(taskName);
        resolveFutureWorkInfos(future, promise);
    }

    @ReactMethod
    public void cancelWorkById(String uuid, final Promise promise) {
        UUID workerId = UUID.fromString(uuid);
        Operation operation = workManager.cancelWorkById(workerId);
        resolveOperation(operation, promise);
    }

    @ReactMethod
    public void cancelUniqueWork(String taskName, final Promise promise) {
        Operation operation = workManager.cancelUniqueWork(taskName);
        resolveOperation(operation, promise);
    }

    @ReactMethod
    public void cancelAllWork(final Promise promise) {
        Operation operation = workManager.cancelAllWork();
        resolveOperation(operation, promise);
    }

    private UUID enqueueOneTimeWork(String taskKey, ReadableMap params, Boolean isUnique) throws MissingParameterException, UnknownExistingWorkPolicyException {
        OneTimeWorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(Worker.class);

        Data inputData = inputData(taskKey, params);
        workRequestBuilder.setInputData(inputData);
        OneTimeWorkRequest workRequest = workRequestBuilder.build();

        if (isUnique) {
            checkKey(params, Constants.EXISTING_WORK_POLICY);
            String existingWorkPolicyString = params.getString(Constants.EXISTING_WORK_POLICY);
            ExistingWorkPolicy existingWorkPolicy;
            switch (existingWorkPolicyString) {
                case Constants.KEEP:
                    existingWorkPolicy = ExistingWorkPolicy.KEEP;
                    break;
                case Constants.REPLACE:
                    existingWorkPolicy = ExistingWorkPolicy.REPLACE;
                    break;
                default:
                    throw new UnknownExistingWorkPolicyException(existingWorkPolicyString);
            }
            workManager.enqueueUniqueWork(taskKey, existingWorkPolicy, workRequest);
        } else {
            workManager.enqueue(workRequest);
        }

        return workRequest.getId();
    }

    private UUID enqueuePeriodicWork(String taskKey, ReadableMap params, Boolean isUnique) throws MissingParameterException, UnknownTimeUnitException, UnknownExistingWorkPolicyException {
        checkKey(params, Constants.INTERVAL);
        Integer interval = params.getInt(Constants.INTERVAL);

        checkKey(params, Constants.TIME_UNIT);
        String timeUnitString = params.getString(Constants.TIME_UNIT);
        TimeUnit timeUnit = timeUnitFromString(timeUnitString);

        PeriodicWorkRequest.Builder workRequestBuilder = new PeriodicWorkRequest.Builder(
                Worker.class,
                interval,
                timeUnit
        );

        Data inputData = inputData(taskKey, params);
        workRequestBuilder.setInputData(inputData);
        PeriodicWorkRequest workRequest = workRequestBuilder.build();

        if (isUnique) {
            checkKey(params, Constants.EXISTING_WORK_POLICY);
            String existingWorkPolicyString = params.getString(Constants.EXISTING_WORK_POLICY);
            ExistingPeriodicWorkPolicy existingWorkPolicy;
            switch (existingWorkPolicyString) {
                case Constants.KEEP:
                    existingWorkPolicy = ExistingPeriodicWorkPolicy.KEEP;
                    break;
                case Constants.REPLACE:
                    existingWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE;
                    break;
                default:
                    throw new UnknownExistingWorkPolicyException(existingWorkPolicyString);
            }
            workManager.enqueueUniquePeriodicWork(taskKey, existingWorkPolicy, workRequest);
        } else {
            workManager.enqueue(workRequest);
        }

        return workRequest.getId();
    }

    private Data inputData(String taskKey, ReadableMap params) throws MissingParameterException {
        checkKey(params, Constants.TIMEOUT);
        Integer timeout = params.getInt(Constants.TIMEOUT);

        Data.Builder inputDataBuilder = new Data
                .Builder()
                .putString(Constants.TASK_KEY, taskKey)
                .putInt(Constants.TIMEOUT, timeout);

        if (params.hasKey(Constants.DATA)) {
            ReadableMap data = params.getMap(Constants.DATA);
            inputDataBuilder.putAll(data.toHashMap());
        }

        return inputDataBuilder.build();
    }

    private void executeFuture(ListenableFuture future, FutureCallback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        Futures.addCallback(future, callback, executor);
    }

    private void resolveOperation(Operation operation, final Promise promise) {
        ListenableFuture<Operation.State.SUCCESS> future = operation.getResult();
        executeFuture(future, new FutureCallback<Operation.State.SUCCESS>() {
            @Override
            public void onSuccess(@NullableDecl Operation.State.SUCCESS result) {
                promise.resolve(null);
            }

            @Override
            public void onFailure(Throwable t) {
                promise.reject(t);
            }
        });
    }

    private void resolveFutureWorkInfo(ListenableFuture<WorkInfo> future, final Promise promise) {
        executeFuture(future, new FutureCallback<WorkInfo>() {
            @Override
            public void onSuccess(@NullableDecl WorkInfo result) {
                WritableMap map = workInfoMap(result);
                promise.resolve(map);
            }

            @Override
            public void onFailure(Throwable t) {
                promise.reject(t);
            }
        });
    }

    private void resolveFutureWorkInfos(ListenableFuture<List<WorkInfo>> future, final Promise promise) {
        executeFuture(future, new FutureCallback<List<WorkInfo>>() {
            @Override
            public void onSuccess(@NullableDecl List<WorkInfo> result) {
                WritableArray array = Arguments.createArray();
                for (WorkInfo workInfo : result) {
                    WritableMap map = workInfoMap(workInfo);
                    array.pushMap(map);
                }
                promise.resolve(array);
            }

            @Override
            public void onFailure(Throwable t) {
                promise.reject(t);
            }
        });
    }

    private WritableMap workInfoMap(WorkInfo workInfo) {
        WritableMap map = Arguments.createMap();
        map.putString(Constants.ID, workInfo.getId().toString());
        map.putString(Constants.STATE, workInfo.getState().toString());

        Map<String, Object> outputDataMap = workInfo.getOutputData().getKeyValueMap();
        WritableMap outputData = Arguments.makeNativeMap(outputDataMap);
        map.putMap(Constants.OUTPUT_DATA, outputData);

        List<String> tagList = new ArrayList<>(workInfo.getTags());
        WritableArray tags = Arguments.makeNativeArray(tagList);
        map.putArray(Constants.TAGS, tags);

        return map;
    }

    private void checkKey(ReadableMap params, String key) throws MissingParameterException {
        if (!params.hasKey(key)) {
            throw new MissingParameterException(key);
        }
    }

    private TimeUnit timeUnitFromString(String string) throws UnknownTimeUnitException {
        switch (string) {
            case Constants.MILLISECONDS:
                return TimeUnit.MILLISECONDS;
            case Constants.SECONDS:
                return TimeUnit.SECONDS;
            case Constants.MINUTES:
                return TimeUnit.MINUTES;
            case Constants.HOURS:
                return TimeUnit.HOURS;
            case Constants.DAYS:
                return TimeUnit.DAYS;
            default:
                throw new UnknownTimeUnitException(string);
        }
    }
}
