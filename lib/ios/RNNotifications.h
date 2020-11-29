@import UIKit;
#import <PushKit/PushKit.h>
@import UserNotifications;

@interface RNNotifications : NSObject

+ (instancetype)sharedInstance;

+ (void)startMonitorNotifications;
+ (void)startMonitorBackgroundNotifications:(NSDictionary *)payload;
+ (void)startMonitorPushKitNotifications;

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSDictionary *)tokens;
+ (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;

+ (void)addNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate;
+ (void)removeNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate;

@end
