#import "RNCommandsHandler.h"
#import "RNNotifications.h"
#import "RNNotificationsStore.h"
#import "RNNotificationsStorage.h"
#import "RCTConvert+RNNotifications.h"

@implementation RNCommandsHandler {
    RNNotificationCenter* _notificationCenter;
    RNNotificationsStorage* _notificationStorage;
}

- (instancetype)init {
    self = [super init];
    _notificationCenter = [RNNotificationCenter new];
    _notificationStorage = [RNNotificationsStorage new];
    return self;
}

- (void)requestPermissions {
    [_notificationCenter requestPermissions];
}

- (void)setCategories:(NSArray *)categories {
    [_notificationCenter setCategories:categories];
}

- (void)getInitialNotification:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    resolve([[RNNotificationsStore sharedInstance] initialNotification]);
}

- (void)getInitialAction:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    resolve([[RNNotificationsStore sharedInstance] initialAction]);
}

- (void)finishHandlingAction:(NSString *)completionKey {
    [[RNNotificationsStore sharedInstance] completeAction:completionKey];
}

- (void)finishPresentingNotification:(NSDictionary *)notification presentingOptions:(NSDictionary *)presentingOptions {
    NSString *completionKey = [notification valueForKey:@"identifier"];
    [[RNNotificationsStore sharedInstance] completePresentation:completionKey withPresentationOptions:[RCTConvert UNNotificationPresentationOptions:presentingOptions]];
    if ([presentingOptions valueForKey:@"alert"]) {
        NSDictionary *payload = [notification valueForKey:@"payload"];
        [_notificationStorage saveMFA:payload];
    }
}

- (void)abandonPermissions {
    [[UIApplication sharedApplication] unregisterForRemoteNotifications];
}

- (void)registerPushKit {
    [RNNotifications startMonitorPushKitNotifications];
}

- (void)getBadgeCount:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    NSInteger count = [UIApplication sharedApplication].applicationIconBadgeNumber;
    resolve([NSNumber numberWithInteger:count]);
}

- (void)setBadgeCount:(int)count {
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:count];
}

- (void)postLocalNotification:(NSDictionary *)notification withId:(NSNumber *)notificationId {
    [_notificationCenter postLocalNotification:notification withId:notificationId];
}

- (void)cancelLocalNotification:(NSNumber *)notificationId {
    [_notificationCenter cancelLocalNotification:notificationId];
}

- (void)cancelAllLocalNotifications {
    [_notificationCenter cancelAllLocalNotifications];
}

- (void)isRegisteredForRemoteNotifications:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [_notificationCenter isRegisteredForRemoteNotifications:resolve];
}

- (void)checkPermissions:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [_notificationCenter checkPermissions:resolve];
}

- (void)removeAllDeliveredNotifications {
    [_notificationCenter removeAllDeliveredNotifications];
    [_notificationStorage clearAll];
}

- (void)removeDeliveredNotifications:(NSArray<NSString *> *)requestIds resolve:(RCTPromiseResolveBlock)resolve {
    [_notificationCenter removeDeliveredNotifications:requestIds withResolve:resolve];
}

- (void)dismissNotification:(NSString *)requestId {
    [_notificationCenter dismissNotification:requestId];
}

- (void)getPendingMFAs:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [_notificationCenter getDeliveredNotifications:resolve];
}

- (void)updateMFA:(NSDictionary *)mfa answer:(BOOL *)answer resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [_notificationStorage updateMFA:mfa answer:answer];
    resolve(@"success");
}

- (void)isMfaAnswered:(NSString *)requestId resolve:(RCTPromiseResolveBlock)resolve {
    BOOL hasAnswered = *[_notificationStorage isMfaAnswered:requestId];
    resolve(@(hasAnswered));
}

- (void)saveFetchedMFAs:(NSArray *)fetchedMFAs resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [_notificationStorage saveFetchedMFAs:fetchedMFAs];
    resolve(@"success");
}

@end
