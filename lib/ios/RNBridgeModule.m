#import "RNBridgeModule.h"
#import "RNCommandsHandler.h"
#import "RCTConvert+RNNotifications.h"
#import "RNNotificationsStore.h"
#import <React/RCTBridgeDelegate.h>
#import <React/RCTBridge.h>

@implementation RNBridgeModule {
    RNCommandsHandler* _commandsHandler;
}

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    _commandsHandler = [[RNCommandsHandler alloc] init];
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (void)setBridge:(RCTBridge *)bridge {
    _bridge = bridge;
    if ([_bridge.launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]) {
        [[RNNotificationsStore sharedInstance] setInitialNotification:[_bridge.launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]];
    }
}

#pragma mark - JS interface

RCT_EXPORT_METHOD(requestPermissions) {
    [_commandsHandler requestPermissions];
}

RCT_EXPORT_METHOD(setCategories:(NSArray *)categories) {
    [_commandsHandler setCategories:categories];
}

RCT_EXPORT_METHOD(getInitialNotification:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler getInitialNotification:resolve reject:reject];
}

RCT_EXPORT_METHOD(getInitialAction:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler getInitialAction:resolve reject:reject];
}

RCT_EXPORT_METHOD(finishHandlingAction:(NSString *)completionKey) {
    [_commandsHandler finishHandlingAction:completionKey];
}

RCT_EXPORT_METHOD(finishPresentingNotification:(NSDictionary *)notification presentingOptions:(NSDictionary *)presentingOptions) {
    [_commandsHandler finishPresentingNotification:notification presentingOptions:presentingOptions];
}

RCT_EXPORT_METHOD(abandonPermissions) {
    [_commandsHandler abandonPermissions];
}

RCT_EXPORT_METHOD(registerPushKit) {
    [_commandsHandler registerPushKit];
}

RCT_EXPORT_METHOD(getBadgeCount:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler getBadgeCount:resolve reject:reject];
}

RCT_EXPORT_METHOD(setBadgeCount:(int)count) {
    [_commandsHandler setBadgeCount:count];
}

RCT_EXPORT_METHOD(postLocalNotification:(NSDictionary *)notification withId:(nonnull NSNumber *)notificationId) {
    [_commandsHandler postLocalNotification:notification withId:notificationId];
}

RCT_EXPORT_METHOD(cancelLocalNotification:(nonnull NSNumber *)notificationId) {
    [_commandsHandler cancelLocalNotification:notificationId];
}

RCT_EXPORT_METHOD(cancelAllLocalNotifications) {
    [_commandsHandler cancelAllLocalNotifications];
}

RCT_EXPORT_METHOD(isRegisteredForRemoteNotifications:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler isRegisteredForRemoteNotifications:resolve reject:reject];
}

RCT_EXPORT_METHOD(checkPermissions:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler checkPermissions:resolve reject:reject];
}

#if !TARGET_OS_TV

RCT_EXPORT_METHOD(removeAllDeliveredNotifications) {
    [_commandsHandler removeAllDeliveredNotifications];
}

RCT_EXPORT_METHOD(removeDeliveredNotifications:(NSArray<NSString *> *)requestIds resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler removeDeliveredNotifications:requestIds resolve:resolve];
}

RCT_EXPORT_METHOD(dismissNotification:(NSString *)requestId) {
    [_commandsHandler dismissNotification:requestId];
}

RCT_EXPORT_METHOD(getPendingMfas:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler getPendingMfas:resolve reject:reject];
}

RCT_EXPORT_METHOD(updateMfa:(NSDictionary *)mfa answer:(BOOL *)answer resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler updateMfa:mfa answer:answer resolve:resolve reject:reject];
}

RCT_EXPORT_METHOD(isMfaAnswered:(NSString *)requestId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler isMfaAnswered:requestId resolve:resolve reject:reject];
}

RCT_EXPORT_METHOD(saveFetchedMfas:(NSArray *)fetchedMfas resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler saveFetchedMfas:fetchedMfas resolve:resolve reject:reject];
}

#endif

@end

