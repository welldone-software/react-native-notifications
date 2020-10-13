#import "RNNotificationCenterListener.h"
#import "RCTConvert+RNNotifications.h"
#import "RNNotificationsStorage.h"

@import UIKit;

@implementation RNNotificationCenterListener {
    RNNotificationEventHandler* _notificationEventHandler;
    RNNotificationsStorage* _notificationStorage;
}

- (instancetype)initWithNotificationEventHandler:(RNNotificationEventHandler *)notificationEventHandler {
    self = [super init];
    _notificationEventHandler = notificationEventHandler;
    _notificationStorage = [RNNotificationsStorage new];
    return self;
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    if([[UIApplication sharedApplication] applicationState] != UIApplicationStateActive) {
        [_notificationStorage saveNotification:notification];
    }
    [_notificationEventHandler didReceiveForegroundNotification:notification withCompletionHandler:completionHandler];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler {
    [_notificationEventHandler didReceiveNotificationResponse:response completionHandler:completionHandler];
}

@end
