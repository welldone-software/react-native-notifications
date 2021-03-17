import * as _ from 'lodash';
import {NativeCommandsSender} from '../adapters/NativeCommandsSender';
import {Notification} from '../DTO/Notification';
import {NotificationCategory} from '../interfaces/NotificationCategory';
import {NotificationChannel} from '../interfaces/NotificationChannel';
import {NotificationPermissions} from '../interfaces/NotificationPermissions';
import {UniqueIdProvider} from '../adapters/UniqueIdProvider';
import {NotificationFactory} from '../DTO/NotificationFactory';
import {NotificationActionResponse} from '../interfaces/NotificationActionResponse';
import {Platform} from 'react-native';

export class Commands {
  constructor(
    private readonly nativeCommandsSender: NativeCommandsSender,
    private readonly uniqueIdProvider: UniqueIdProvider,
    private readonly notificationFactory: NotificationFactory
  ) {}

  public postLocalNotification(notification: Notification, id?: number) {
    const notificationId: number = id ? id : this.uniqueIdProvider.generate();
    this.nativeCommandsSender.postLocalNotification(
      notification,
      notificationId
    );
    return notificationId;
  }

  public async getInitialNotification(): Promise<Notification | undefined> {
    return this.nativeCommandsSender
      .getInitialNotification()
      .then((payload) => {
        if (payload) {
          return this.notificationFactory.fromPayload(payload);
        }

        return undefined;
      });
  }

  public async getInitialAction(): Promise<Notification | undefined> {
    if (Platform.OS === 'android') {
      return this.getInitialNotification();
    }
    return this.nativeCommandsSender.getInitialAction().then((response) => {
      if (response?.notification) {
        const action = response.action
          ? new NotificationActionResponse(response.action).identifier
          : undefined;
        return this.notificationFactory.fromPayload({
          ...response.notification,
          action,
        });
      }

      return undefined;
    });
  }

  public requestPermissions() {
    const result = this.nativeCommandsSender.requestPermissions();
    return result;
  }

  public abandonPermissions() {
    const result = this.nativeCommandsSender.abandonPermissions();
    return result;
  }

  public registerPushKit() {
    this.nativeCommandsSender.registerPushKit();
  }

  public setCategories(categories: [NotificationCategory?]) {
    this.nativeCommandsSender.setCategories(categories);
  }

  public getBadgeCount(): Promise<number> {
    return this.nativeCommandsSender.getBadgeCount();
  }

  public setBadgeCount(count: number) {
    this.nativeCommandsSender.setBadgeCount(count);
  }

  public cancelLocalNotification(notificationId: string) {
    this.nativeCommandsSender.cancelLocalNotification(notificationId);
  }

  public dismissNotification(notificationId: string) {
    this.nativeCommandsSender.dismissNotification(notificationId);
  }

  public cancelAllLocalNotifications() {
    this.nativeCommandsSender.cancelAllLocalNotifications();
  }

  public isRegisteredForRemoteNotifications(): Promise<boolean> {
    return this.nativeCommandsSender.isRegisteredForRemoteNotifications();
  }

  public checkPermissions(): Promise<NotificationPermissions> {
    return this.nativeCommandsSender.checkPermissions();
  }

  public removeAllDeliveredNotifications() {
    this.nativeCommandsSender.removeAllDeliveredNotifications();
  }

  public removeDeliveredNotifications(identifiers: Array<string>) {
    return this.nativeCommandsSender.removeDeliveredNotifications(identifiers);
  }

  public getPendingMfas(): Promise<Notification[]> {
    return this.nativeCommandsSender.getPendingMfas();
  }

  public updateMfa(
    mfa: any & {mfa_request_id: string; answer: boolean},
    answer: boolean
  ): Promise<void> {
    return this.nativeCommandsSender.updateMfa(mfa, answer);
  }

  public isMfaAnswered(requestId: string) {
    return this.nativeCommandsSender.isMfaAnswered(requestId);
  }

  public saveFetchedMfas(
    fetchedMfas: (any & {mfa_request_id: string})[]
  ): Promise<void> {
    return this.nativeCommandsSender.saveFetchedMfas(fetchedMfas);
  }

  public refreshToken() {
    this.nativeCommandsSender.refreshToken();
  }

  public setNotificationChannel(notificationChannel: NotificationChannel) {
    this.nativeCommandsSender.setNotificationChannel(notificationChannel);
  }
}
