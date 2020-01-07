# react-native-headless-work-manager

Enables the use of Android's [WorkManger](https://developer.android.com/topic/libraries/architecture/workmanager) to schedule execution of [HeadlessJS](https://facebook.github.io/react-native/docs/headless-js-android) tasks.

Once this native module is installed, both the scheduling and definition of tasks can be done entirely in JavaScript.

## Getting started

`$ npm install react-native-headless-work-manager --save`

### Mostly automatic installation

`$ react-native link react-native-headless-work-manager`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.WorkManagerPackage;` to the imports at the top of the file
  - Add `new WorkManagerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-headless-work-manager'
  	project(':react-native-headless-work-manager').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-headless-work-manager/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-headless-work-manager')
  	```


## Usage

To your `AndroidManifest.xml`, add:
```xml
<manifest ...>
	<application ...>
		...
		<service android:name="com.infuse.headlessworkmanager.HeadlessService" />
	</application>
</manifest>
```

`index.js`
```javascript
import { AppRegistry } from 'react-native';

AppRegistry.registerHeadlessTask('YourHeadlessTask', () => data => console.log(data));
```

`App.js` (or wherever you like)
```javascript
import React, { Component } from 'react';
import HeadlessWorkManager from 'react-native-headless-work-manager';

export default class App extends Component {
	async componentDidMount() {
		try {
			const { workerId } = await HeadlessWorkManager.enqueue({
				workRequestType: HeadlessWorkManager.PERIODIC_WORK_REQUEST,
				taskKey: 'YourHeadlessTask',
				isUnique: true,
				existingWorkPolicy: HeadlessWorkManager.REPLACE,
				timeout: 1000,
				interval: 15,
				timeUnit: HeadlessWorkManager.MINUTES,
				data: {
					foo: 'bar',
				},
			});

			const workInfo = await HeadlessWorkManager.getWorkInfosForUniqueWork('YourHeadlessTask');
		} catch(err) {
			console.error(err);
		}
	}
}
```

## API

### HeadlessWorkManager : `Object`

* HeadlessWorkManager : `Object`
	* .enqueue(params) ⇒ `Promise<EnqueueResult>`
	* .getWorkInfoById ⇒ `Promise<WorkInfo>`
	* .getWorkInfosForUniqueWork  ⇒ `Promise<Array<WorkInfo>>`
	* .cancelWorkById(workerId) ⇒ `Promise<null>`
	* .cancelUniqueWork(taskName) ⇒ `Promise<null>`
	* .cancelAllWork() ⇒ `Promise<null>`

#### HeadlessWorkManager.enqueue(params) ⇒ `Promise<EnqueueResult>`

| Param | Type | Description |
| --- | --- | --- |
| params | `Object` |  |
| params.workRequestType | `HeadlessWorkManager.ONE_TIME_WORK_REQUEST` ⎮ `HeadlessWorkManager.PERIODIC_WORK_REQUEST` |  |
| params.taskKey | `string` | The string used to register your task |
| [params.isUnique] | `boolean` | If true, any task with the same key will be replaced |
| [params.existingWorkPolicy] | `HeadlessWorkManager.KEEP` ⎮ `HeadlessWorkManager.REPLACE` | If work is unique and policy is keep, new work will not start. If work is unique and policy is replace, new work will replace existing. |
| params.timeout | `number` | Timeout for execution of your task |
| [params.interval] | `number` | Interval over which your task will be repeated, in given units |
| [params.timeUnit] | `HeadlessWorkManager.MILLISECONDS` ⎮ `HeadlessWorkManager.SECONDS` ⎮ `HeadlessWorkManager.MINUTES` ⎮ `HeadlessWorkManager.HOURS` ⎮ `HeadlessWorkManager.DAYS` | Units for your repeat interval |
| [params.data] | `Object` | Shallow object of data to be passed to your task |

#### HeadlessWorkManager.getWorkInfoById(workerId) ⇒ `Promise<WorkInfo>`

| Param | Type | Description |
| --- | --- | --- |
| workerId | `string` | ID that was provided by the resolution of the promise returned by enqueue |

#### HeadlessWorkManager.getWorkInfosForUniqueWork(taskKey) ⇒ `Promise<Array<WorkInfo>>`

| Param | Type | Description |
| --- | --- | --- |
| taskKey | `string` | The string used to register your task |

#### HeadlessWorkManager.cancelWorkById(workerId) ⇒ `Promise<null>`

| Param | Type | Description |
| --- | --- | --- |
| workerId | `string` | ID that was provided by the resolution of the promise returned by enqueue |

#### HeadlessWorkManager.cancelUniqueWork(taskName) ⇒ `Promise<null>`

| Param | Type |
| --- | --- |
| taskKey | `string` | The string used to register your task |

#### HeadlessWorkManager.cancelAllWork() ⇒ `Promise<null>`

### Response Types

#### EnqueueResult : `Object`

| Param | Type |
| --- | --- |
| workerId | `string` | 

#### WorkInfo : `Object`

| Param | Type |
| --- | --- |
| id | `string` | 
| state | `string` | 
| outputData | `Object` | 
| tags | `Array<string>` | 
