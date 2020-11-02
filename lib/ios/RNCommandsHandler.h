#import <Foundation/Foundation.h>
#import "RNNotificationCenter.h"

@interface RNCommandsHandler : NSObject

- (instancetype)init;

- (void)requestPermissions;

- (void)setCategories:(NSArray *)categories;

- (void)getInitialNotification:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;

- (void)getInitialAction:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;

- (void)finishHandlingAction:(NSString *)completionKey;

- (void)finishPresentingNotification:(NSString *)completionKey presentingOptions:(NSDictionary *)presentingOptions;

- (void)abandonPermissions;

- (void)registerPushKit;

- (void)getBadgeCount:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;

- (void)setBadgeCount:(int)count;

- (void)postLocalNotification:(NSDictionary *)notification withId:(NSNumber *)notificationId;

- (void)cancelLocalNotification:(NSNumber *)notificationId;

- (void)cancelAllLocalNotifications;

- (void)isRegisteredForRemoteNotifications:(RCTPromiseResolveBlock)resolve
                                    reject:(RCTPromiseRejectBlock)reject;

- (void)checkPermissions:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject;

- (void)removeAllDeliveredNotifications;

- (void)removeDeliveredNotifications:(NSArray<NSString *> *)requestIds;

- (void)dismissNotification:(NSString *)requestId;

- (void)getDeliveredNotifications:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;

@end
