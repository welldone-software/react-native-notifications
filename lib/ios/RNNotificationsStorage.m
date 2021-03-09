#import <Foundation/Foundation.h>
#import "RCTConvert+RNNotifications.h"
#import "RNNotificationsStorage.h"
#import "RNNotificationParser.h"

@implementation RNNotificationsStorage

NSUserDefaults *userDefaults;
NSString *NOTIFICATIONS_KEY = @"Notifications";
NSString *MFA_ORDER_KEY = @"MFA Order";
NSString *ANSWER_KEY = @"answer";
NSString *EXPIRED_TIME_KEY = @"expired_time";
NSString *REQUEST_ID_KEY = @"mfa_request_id";
NSString *IDENTIFIER_KEY = @"identifier";

int MFA_SAVE_LIMIT = 256;

- (instancetype) init {
    self = [super init];
    userDefaults = [NSUserDefaults standardUserDefaults];
    return self;
}

- (NSMutableDictionary*)clearLimit:(NSMutableDictionary*) mfas order:(NSMutableArray*) order {
    int overLimitCount = (int)[mfas count] - MFA_SAVE_LIMIT;
    if (overLimitCount > 0) {
        int deletedCount = 0;
        for (NSString *requestId in order) {
            [order removeObject:requestId];
            if ([mfas objectForKey:requestId] != nil) {
                [mfas removeObjectForKey:requestId];
                deletedCount = deletedCount + 1;
                if (overLimitCount <= deletedCount) {
                    break;
                }
            }
        }
    }
    return mfas;
}

-(void)dismissNotificaitons:(NSArray <NSString *> *)requestIds {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center getDeliveredNotificationsWithCompletionHandler:^(NSArray<UNNotification *> * _Nonnull notifications) {
        for (UNNotification *notification in notifications) {
            NSDictionary * parsedNotification = [RCTConvert UNNotificationPayload:notification];
            NSString * identifier = [parsedNotification valueForKey:IDENTIFIER_KEY];
            NSString * requestId = [parsedNotification valueForKey:REQUEST_ID_KEY];
            if ([requestIds containsObject:requestId]) {
                NSMutableArray<NSString *> *notificationIds = [NSMutableArray new];
                [notificationIds addObject:identifier];
                [center removeDeliveredNotificationsWithIdentifiers:notificationIds];
                break;
            }
        }
    }];
}

- (void)saveMFA:(NSDictionary *)mfa{
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    if (mfasDict == nil) {
        mfasDict = [NSMutableDictionary new];
    }
    
    NSMutableArray* mfaOrder = [[userDefaults arrayForKey:MFA_ORDER_KEY] mutableCopy];
    if (mfaOrder == nil) {
        mfaOrder = [NSMutableArray new];
    }
    
    NSString* requestId = [mfa valueForKey:REQUEST_ID_KEY];
    
    if ([mfaOrder containsObject:requestId] || [mfasDict objectForKey:requestId] != nil) {
        return;
    }
    [mfaOrder addObject:requestId];
    [mfasDict setObject:mfa forKey:requestId];
    
    [userDefaults setObject:[self clearLimit:mfasDict order:mfaOrder] forKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
    
    [userDefaults setObject:mfaOrder forKey:MFA_ORDER_KEY];
    [userDefaults synchronize];
}

- (void) updateMFA:(NSString *) requestId answer:(BOOL *) answer; {
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    NSMutableDictionary* mfa = [[mfasDict valueForKey:requestId] mutableCopy];
    [mfa setObject:[NSNumber numberWithBool:answer] forKey:ANSWER_KEY];
    [mfasDict setObject:mfa forKey:requestId];
    [userDefaults setObject:mfasDict forKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
    
    [self dismissNotificaitons:@[requestId]];
}

- (void)saveFetchedMFAs:(NSArray<NSDictionary *> *)fetchedMFAs {
    __block BOOL hasSavedAny = NO;
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    if (mfasDict == nil) {
        mfasDict = [NSMutableDictionary new];
    }
    
    NSMutableArray* mfaOrder = [[userDefaults arrayForKey:MFA_ORDER_KEY] mutableCopy];
    if (mfaOrder == nil) {
        mfaOrder = [NSMutableArray new];
    }
    
    [fetchedMFAs enumerateObjectsUsingBlock:^(NSDictionary * value, NSUInteger idx, BOOL *stop) {
        NSString *requestId = [value valueForKey:REQUEST_ID_KEY];
        if ([mfasDict objectForKey:requestId] == nil) {
            hasSavedAny = YES;
            [mfaOrder addObject:requestId];
            [mfasDict setObject:value forKey:requestId];
        }
    }];
    
    if (hasSavedAny) {
        [userDefaults setObject:[self clearLimit:mfasDict order:mfaOrder] forKey:NOTIFICATIONS_KEY];
        [userDefaults synchronize];
        
        [userDefaults setObject:mfaOrder forKey:MFA_ORDER_KEY];
        [userDefaults synchronize];
    }
}

- (NSMutableArray <NSDictionary *> *) getPendingMFAs {
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    if (mfasDict == nil) {
        mfasDict = [NSMutableDictionary new];
    }
    NSMutableArray <NSDictionary *> *pendingMFAs = [[NSMutableArray alloc] init];
    NSMutableArray <NSString *> *requestIdsToDismiss = [[NSMutableArray alloc] init];
    
    [mfasDict enumerateKeysAndObjectsUsingBlock:^(id key, NSDictionary * value, BOOL* stop) {
        bool hasNotAnswered = [value objectForKey:ANSWER_KEY] == nil;
        
        double currentTsRaw = [[NSDate date] timeIntervalSince1970] * 1000;
        NSString * expiredTime = [value valueForKey:EXPIRED_TIME_KEY];
        bool hasNotExpired = [expiredTime longLongValue] > (long)currentTsRaw;
        
        if (hasNotAnswered && hasNotExpired) {
            [pendingMFAs addObject:value];
        } else {
            [requestIdsToDismiss addObject:[value objectForKey:REQUEST_ID_KEY]];
        }
    }];
    
    [self dismissNotificaitons:requestIdsToDismiss];
    
    return pendingMFAs;
}

- (void) clearAll {
    [userDefaults removeObjectForKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
}

@end
