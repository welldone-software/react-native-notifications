#import <Foundation/Foundation.h>
#import "RNNotificationCenter.h"

@import UserNotifications;

@interface RNNotificationsStorage : NSObject

- (void) saveNotification:(NSDictionary *) notification;
- (void) removeDeliveredNotifications:(NSArray<NSString *> *)identifiers;
- (void) getDeliveredNotifications:(RCTPromiseResolveBlock) resolve;
- (void) clearAll;

@end
