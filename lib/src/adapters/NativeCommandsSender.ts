import {NativeModules} from 'react-native';
import {Notification} from '../DTO/Notification';
import {NotificationCompletion} from '../interfaces/NotificationCompletion';
import {NotificationPermissions} from '../interfaces/NotificationPermissions';
import {NotificationCategory} from '../interfaces/NotificationCategory';
import {NotificationChannel} from '../interfaces/NotificationChannel';

interface NativeCommandsModule {
  getInitialNotification(): Promise<Object>;
  getInitialAction(): Promise<{notification: any; action: any}>;
  postLocalNotification(notification: Notification, id: number): void;
  requestPermissions(): void;
  abandonPermissions(): void;
  refreshToken(): void;
  registerPushKit(): void;
  getBadgeCount(): Promise<number>;
  setBadgeCount(count: number): void;
  cancelLocalNotification(notificationId: string): void;
  dismissNotification(notificationId: string): void;
  cancelAllLocalNotifications(): void;
  isRegisteredForRemoteNotifications(): Promise<boolean>;
  checkPermissions(): Promise<NotificationPermissions>;
  removeDeliveredNotifications(identifiers: Array<string>): void;
  removeAllDeliveredNotifications(): void;
  getPendingMFAs(): Promise<Notification[] | string>;
  updateMFA(
    mfa: any & {mfa_request_id: string; answer: boolean},
    answer: boolean
  ): Promise<void>;
  saveFetchedMFAs(fetchedMFAs: any[]): Promise<void>;
  setCategories(categories: [NotificationCategory?]): void;
  finishPresentingNotification(
    notification: Notification,
    callback: NotificationCompletion
  ): void;
  finishHandlingAction(notificationId: string): void;
  setNotificationChannel(notificationChannel: NotificationChannel): void;
}

export class NativeCommandsSender {
  private readonly nativeCommandsModule: NativeCommandsModule;
  constructor() {
    this.nativeCommandsModule = NativeModules.RNBridgeModule;
  }

  postLocalNotification(notification: Notification, id: number) {
    return this.nativeCommandsModule.postLocalNotification(
      {...notification.payload},
      id
    );
  }

  getInitialNotification(): Promise<Object> {
    return this.nativeCommandsModule.getInitialNotification();
  }

  getInitialAction(): Promise<{notification: any; action: any}> {
    return this.nativeCommandsModule.getInitialAction();
  }

  requestPermissions() {
    return this.nativeCommandsModule.requestPermissions();
  }

  abandonPermissions() {
    return this.nativeCommandsModule.abandonPermissions();
  }

  refreshToken() {
    this.nativeCommandsModule.refreshToken();
  }

  registerPushKit() {
    return this.nativeCommandsModule.registerPushKit();
  }

  setCategories(categories: [NotificationCategory?]) {
    this.nativeCommandsModule.setCategories(categories);
  }

  getBadgeCount(): Promise<number> {
    return this.nativeCommandsModule.getBadgeCount();
  }

  setBadgeCount(count: number) {
    this.nativeCommandsModule.setBadgeCount(count);
  }

  cancelLocalNotification(notificationId: string) {
    this.nativeCommandsModule.cancelLocalNotification(notificationId);
  }

  dismissNotification(notificationId: string) {
    this.nativeCommandsModule.dismissNotification(notificationId);
  }

  cancelAllLocalNotifications() {
    this.nativeCommandsModule.cancelAllLocalNotifications();
  }

  isRegisteredForRemoteNotifications(): Promise<any> {
    return this.nativeCommandsModule.isRegisteredForRemoteNotifications();
  }

  checkPermissions() {
    return this.nativeCommandsModule.checkPermissions();
  }

  removeAllDeliveredNotifications() {
    return this.nativeCommandsModule.removeAllDeliveredNotifications();
  }

  removeDeliveredNotifications(identifiers: Array<string>) {
    return this.nativeCommandsModule.removeDeliveredNotifications(identifiers);
  }

  public async getPendingMFAs(): Promise<Notification[]> {
    const result = await this.nativeCommandsModule.getPendingMFAs();
    const payloadArray =
      typeof result === 'string' ? JSON.parse(result) : result;
    return payloadArray.map((payload: object) => new Notification(payload));
  }

  updateMFA(
    mfa: any & {mfa_request_id: string; answer: boolean},
    answer: boolean
  ) {
    return this.nativeCommandsModule.updateMFA(mfa, answer);
  }

  saveFetchedMFAs(fetchedMFAs: (any & {mfa_request_id: string})[]) {
    return this.nativeCommandsModule.saveFetchedMFAs(fetchedMFAs);
  }

  finishPresentingNotification(
    notification: Notification,
    notificationCompletion: NotificationCompletion
  ): void {
    this.nativeCommandsModule.finishPresentingNotification(
      notification,
      notificationCompletion
    );
  }

  finishHandlingAction(notificationId: string): void {
    this.nativeCommandsModule.finishHandlingAction(notificationId);
  }

  setNotificationChannel(notificationChannel: NotificationChannel) {
    this.nativeCommandsModule.setNotificationChannel(notificationChannel);
  }
}
