#import "RNNotificationCenter.h"
#import "RCTConvert+RNNotifications.h"
#import "RNNotificationsStorage.h"

@implementation RNNotificationCenter

- (void)requestPermissions {
    UNAuthorizationOptions authOptions = (UNAuthorizationOptionBadge | UNAuthorizationOptionSound | UNAuthorizationOptionAlert);
    [UNUserNotificationCenter.currentNotificationCenter requestAuthorizationWithOptions:authOptions completionHandler:^(BOOL granted, NSError * _Nullable error) {
        if (!error && granted) {
            [UNUserNotificationCenter.currentNotificationCenter getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
                if (settings.authorizationStatus == UNAuthorizationStatusAuthorized) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[UIApplication sharedApplication] registerForRemoteNotifications];
                    });
                }
            }];
        }
    }];
}

- (void)setCategories:(NSArray *)json {
    NSMutableSet<UNNotificationCategory *>* categories = nil;
    
    if ([json count] > 0) {
        categories = [NSMutableSet new];
        for (NSDictionary* categoryJson in json) {
            [categories addObject:[RCTConvert UNMutableUserNotificationCategory:categoryJson]];
        }
    }
    [[UNUserNotificationCenter currentNotificationCenter] setNotificationCategories:categories];
}

- (void)postLocalNotification:(NSDictionary *)notification withId:(NSNumber *)notificationId {
    UNNotificationRequest* localNotification = [RCTConvert UNNotificationRequest:notification withId:notificationId];
    [[UNUserNotificationCenter currentNotificationCenter] addNotificationRequest:localNotification withCompletionHandler:nil];
}

- (void)cancelLocalNotification:(NSNumber *)notificationId {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center removePendingNotificationRequestsWithIdentifiers:@[[notificationId stringValue]]];
}

- (void)removeAllDeliveredNotifications {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center removeAllDeliveredNotifications];
}

- (void)removeDeliveredNotifications:(NSArray<NSString *> *)requestIds withResolve:(RCTPromiseResolveBlock)resolve {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getDeliveredNotificationsWithCompletionHandler:^(NSArray<UNNotification *> * _Nonnull notifications) {
        NSMutableArray<NSString *> *notificationIds = [NSMutableArray new];
        for (UNNotification *notification in notifications) {
            NSDictionary * parsedNotification = [RCTConvert UNNotificationPayload:notification];
            NSString * identifier = [parsedNotification valueForKey:@"identifier"];
            NSString * mfaRequestId = [parsedNotification valueForKey:@"mfa_request_id"];
            if ([requestIds containsObject:mfaRequestId]) {
                [notificationIds addObject:identifier];
            }
        }
        [center removeDeliveredNotificationsWithIdentifiers:notificationIds];
        resolve(@"success");
    }];
}

- (void)dismissNotification:(NSString *)requestId {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getDeliveredNotificationsWithCompletionHandler:^(NSArray<UNNotification *> * _Nonnull notifications) {
        for (UNNotification *notification in notifications) {
            NSDictionary * parsedNotification = [RCTConvert UNNotificationPayload:notification];
            NSString * identifier = [parsedNotification valueForKey:@"identifier"];
            NSString * mfaRequestId = [parsedNotification valueForKey:@"mfa_request_id"];
            if ([requestId isEqualToString:mfaRequestId]) {
                NSMutableArray<NSString *> *notificationIds = [NSMutableArray new];
                [notificationIds addObject:identifier];
                [center removeDeliveredNotificationsWithIdentifiers:notificationIds];
                break;
            }
        }
    }];
}

- (void)getDeliveredNotifications:(RCTPromiseResolveBlock)resolve {
    RNNotificationsStorage *notificationStorage = [RNNotificationsStorage new];
    NSMutableArray<NSDictionary *> *formattedNotifications = [notificationStorage getPendingMFAs];
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getDeliveredNotificationsWithCompletionHandler:^(NSArray<UNNotification *> * _Nonnull notifications) {
        for (UNNotification *notification in notifications) {
            [formattedNotifications addObject:[RCTConvert UNNotificationPayload:notification]];
        }
        resolve(formattedNotifications);
    }];
}

- (void)cancelAllLocalNotifications {
    [[UNUserNotificationCenter currentNotificationCenter] removeAllPendingNotificationRequests];
}

- (void)isRegisteredForRemoteNotifications:(RCTPromiseResolveBlock)resolve {
    [[UNUserNotificationCenter currentNotificationCenter] getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
        if (settings.alertSetting == UNNotificationSettingEnabled || settings.soundSetting == UNNotificationSettingEnabled || settings.badgeSetting == UNNotificationSettingEnabled) {
            resolve(@(YES));
        } else {
            resolve(@(NO));
        }
    }];
}

- (void)checkPermissions:(RCTPromiseResolveBlock)resolve {
    [[UNUserNotificationCenter currentNotificationCenter] getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
        resolve(@{
                  @"badge": @(settings.badgeSetting == UNNotificationSettingEnabled),
                  @"sound": @(settings.soundSetting == UNNotificationSettingEnabled),
                  @"alert": @(settings.alertSetting == UNNotificationSettingEnabled),
                  });
    }];
}

@end
