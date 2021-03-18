#import <UIKit/UIKit.h>
#import <PushKit/PushKit.h>
#import <UserNotifications/UserNotifications.h>

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
