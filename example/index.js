/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';

AppRegistry.registerHeadlessTask('ExampleTask', () => data =>
  console.log(`ExampleTask executed with data: ${JSON.stringify(data)}`),
);
AppRegistry.registerComponent(appName, () => App);
