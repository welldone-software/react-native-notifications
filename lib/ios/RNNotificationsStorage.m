#import <Foundation/Foundation.h>
#import "RNNotificationsStorage.h"
#import "RNNotificationParser.h"

@implementation RNNotificationsStorage

NSUserDefaults *userDefaults;
NSString *notificationsKey = @"Notifications";

- (instancetype) init {
    self = [super init];
    userDefaults = [NSUserDefaults standardUserDefaults];
    return self;
}

- (void)saveNotification:(NSDictionary *)notification{
    NSMutableDictionary* notificationsDict = [[userDefaults dictionaryForKey:notificationsKey] mutableCopy];
    if (notificationsDict == nil) {
        notificationsDict = [NSMutableDictionary new];
    }
    NSString* notificationId = [notification valueForKey:@"mfa_request_id"];
    [notificationsDict setObject:notification forKey:notificationId];
    [userDefaults setObject:notificationsDict forKey:notificationsKey];
    [userDefaults synchronize];
}

- (void) removeDeliveredNotifications:(NSArray<NSString *> *)identifiers {
    NSMutableDictionary* notificationsDict = [[userDefaults dictionaryForKey:notificationsKey] mutableCopy];
    for (id identifier in identifiers) {
        [notificationsDict removeObjectForKey:identifier];
    }
    [userDefaults setObject:notificationsDict forKey:notificationsKey];
    [userDefaults synchronize];
}

- (NSMutableArray <NSDictionary *> *) getDeliveredNotifications {
    NSMutableDictionary* notificationsDict = [[userDefaults dictionaryForKey:notificationsKey] mutableCopy];
    NSMutableArray <NSDictionary *> *deliveredNotifications = [[NSMutableArray alloc] init];
    [notificationsDict enumerateKeysAndObjectsUsingBlock:^(id key, id value, BOOL* stop) {
        [deliveredNotifications addObject:value];
    }];
    return deliveredNotifications;
}

- (void) clearAll {
    [userDefaults removeObjectForKey:notificationsKey];
    [userDefaults synchronize];
}

@end
