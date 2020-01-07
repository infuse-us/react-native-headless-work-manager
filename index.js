import { NativeModules } from 'react-native';

const { HeadlessWorkManager } = NativeModules;

/**
 * @namespace HeadlessWorkManager
 */

/**
 * @typedef {Object} EnqueueResult
 * @param {string} workerId
 */

/**
 * @typedef {Object} WorkInfo
 * @param {string} id
 * @param {string} state
 * @param {Object} outputData
 * @param {Array<string>} tags
 */

/**
 * @function enqueue
 * @memberof HeadlessWorkManager
 * @param {Object} params
 * @param {(HeadlessWorkManager.ONE_TIME_WORK_REQUEST|HeadlessWorkManager.PERIODIC_WORK_REQUEST)} params.workRequestType
 * @param {string} params.taskKey The string used to register your task
 * @param {boolean} [params.isUnique] If true, any task with the same key will be replaced
 * @param {(HeadlessWorkManager.KEEP|HeadlessWorkManager.REPLACE)} [params.existingWorkPolicy] If work is unique and policy is keep, new work will not start. If work is unique and policy is replace, new work will replace existing.
 * @param {number} params.timeout Timeout for execution of your task
 * @param {number} [params.interval] Interval over which your task will be repeated, in given units
 * @param {(HeadlessWorkManager.MILLISECONDS|HeadlessWorkManager.SECONDS|HeadlessWorkManager.MINUTES|HeadlessWorkManager.HOURS|HeadlessWorkManager.DAYS)} [params.timeUnit] Units for your repeat interval
 * @param {Object} [params.data] Shallow object of data to be passed to your task
 * @returns {Promise<EnqueueResult>}
 */

/**
 * @function getWorkInfoById
 * @memberof HeadlessWorkManager
 * @param {string} workerId ID that was provided by the resolution of the promise returned by enqueue
 * @returns {Promise<WorkInfo>}
 */

/**
 * @function getWorkInfosForUniqueWork
 * @memberof HeadlessWorkManager
 * @param {string} taskKey The string used to register your task
 * @returns {Promise<Array<WorkInfo>>}
 */

/**
 * @function cancelWorkById
 * @memberof HeadlessWorkManager
 * @param {string} workerId ID that was provided by the resolution of the promise returned by enqueue
 * @returns {Promise<null>}
 */

 /**
 * @function cancelUniqueWork
 * @memberof HeadlessWorkManager
 * @param {string} taskKey The string used to register your task
 * @returns {Promise<null>}
 */

 /**
 * @function cancelAllWork
 * @memberof HeadlessWorkManager
 * @returns {Promise<null>}
 */

export default HeadlessWorkManager;
