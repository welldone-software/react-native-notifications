import {Notification} from "../DTO/Notification";
import {NotificationActionResponse} from "./NotificationActionResponse";

export interface Registered {
  fcmToken: string;
  apnsToken: string;
}

export interface RegistrationError {
  code: string;
  domain: string;
  localizedDescription: string;
}

export interface RegisteredPushKit {
  pushKitToken: string;
}

export interface NotificationResponse {
  identifier: string;
  notification: Notification;
  action?: NotificationActionResponse;
}
