#import <Foundation/Foundation.h>
#import "RCTConvert+RNNotifications.h"
#import "RNNotificationsStorage.h"
#import "RNNotificationParser.h"

@implementation RNNotificationsStorage

NSUserDefaults *userDefaults;
NSString *NOTIFICATIONS_KEY = @"Notifications";
NSString *Mfa_ORDER_KEY = @"Mfa Order";
NSString *ANSWER_KEY = @"answer";
NSString *EXPIRED_TIME_KEY = @"expired_time";
NSString *REQUEST_ID_KEY = @"mfa_request_id";
NSString *IDENTIFIER_KEY = @"identifier";
NSString *MFA_ARRIVED_TIME_KEY = @"mfa_arrived_time";

int Mfa_SAVE_LIMIT = 256;

- (instancetype) init {
    self = [super init];
    userDefaults = [NSUserDefaults standardUserDefaults];
    return self;
}

- (NSMutableDictionary *) getMfasDict {
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    if (mfasDict == nil) {
        mfasDict = [NSMutableDictionary new];
    }
    return mfasDict;
}

- (long) getCurrentTime {
    double currentTsRaw = [[NSDate date] timeIntervalSince1970] * 1000;
    return (long)currentTsRaw;
}

- (bool) isMfaValid:(NSDictionary *) mfa {
    bool hasNotAnswered = [mfa objectForKey:ANSWER_KEY] == nil;
    NSString * expiredTime = [mfa valueForKey:EXPIRED_TIME_KEY];
    bool hasNotExpired = [expiredTime longLongValue] > [self getCurrentTime];
    return hasNotAnswered && hasNotExpired;
}

- (NSMutableArray *) getMfasOrder {
    NSMutableArray* mfaOrder = [[userDefaults arrayForKey:Mfa_ORDER_KEY] mutableCopy];
    if (mfaOrder == nil) {
        mfaOrder = [NSMutableArray new];
    }
    return mfaOrder;
}

- (NSMutableDictionary*)clearLimit:(NSMutableDictionary*) mfas order:(NSMutableArray*) order {
    int overLimitCount = (int)[mfas count] - Mfa_SAVE_LIMIT;
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

- (void)saveMfa:(NSDictionary *)mfa{
    NSMutableDictionary* mfasDict = [self getMfasDict];
    NSMutableArray* mfaOrder = [self getMfasOrder];
    
    NSString* requestId = [mfa valueForKey:REQUEST_ID_KEY];
    
    NSDictionary *savedMfa = [mfasDict objectForKey:requestId];
    if (savedMfa != nil) {
        if (![self isMfaValid:savedMfa]) {
            [self dismissNotificaitons:@[requestId]];
        }
        return;
    }
    
    NSMutableDictionary *mutableMfa = [mfa mutableCopy];
    [mutableMfa setObject:[NSNumber numberWithLong:[self getCurrentTime]] forKey:MFA_ARRIVED_TIME_KEY];
    [mfaOrder addObject:requestId];
    [mfasDict setObject:mutableMfa forKey:requestId];
    
    [userDefaults setObject:[self clearLimit:mfasDict order:mfaOrder] forKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
    
    [userDefaults setObject:mfaOrder forKey:Mfa_ORDER_KEY];
    [userDefaults synchronize];
}

- (void) updateMfa:(NSDictionary *) mfa answer:(BOOL *) answer; {
    NSMutableDictionary* mfasDict = [self getMfasDict];
    NSString* requestId = [mfa valueForKey:REQUEST_ID_KEY];
    NSDictionary *savedMfa = [mfasDict objectForKey:requestId];
    if (savedMfa != nil && [savedMfa objectForKey:ANSWER_KEY]) {
        return;
    }
    
    NSMutableDictionary* mutableMfa = [mfa mutableCopy];
    [mutableMfa setObject:[NSNumber numberWithBool:answer] forKey:ANSWER_KEY];
    [mfasDict setObject:mutableMfa forKey:requestId];
    
    [userDefaults setObject:mfasDict forKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
    
    [self dismissNotificaitons:@[requestId]];
}

- (BOOL *)isMfaAnswered:(NSString *)requestId {
    NSMutableDictionary* mfasDict = [self getMfasDict];
    NSDictionary *savedMfa = [mfasDict objectForKey:requestId];
    if ([savedMfa objectForKey:ANSWER_KEY] != nil) {
        return YES;
    }
    return NO;
}

- (void)saveFetchedMfas:(NSArray<NSDictionary *> *)fetchedMfas {
    __block BOOL hasSavedAny = NO;
    NSMutableDictionary* mfasDict = [self getMfasDict];
    NSMutableArray* mfaOrder =  [self getMfasOrder];
    
    [fetchedMfas enumerateObjectsUsingBlock:^(NSDictionary * value, NSUInteger idx, BOOL *stop) {
        NSString *requestId = [value valueForKey:REQUEST_ID_KEY];
        if ([mfasDict objectForKey:requestId] == nil) {
            NSMutableDictionary *mutableMfa = [value mutableCopy];
            [mutableMfa setObject:[NSNumber numberWithLong:[self getCurrentTime]] forKey:MFA_ARRIVED_TIME_KEY];
            hasSavedAny = YES;
            [mfaOrder addObject:requestId];
            [mfasDict setObject:mutableMfa forKey:requestId];
        }
    }];
    
    if (hasSavedAny) {
        [userDefaults setObject:[self clearLimit:mfasDict order:mfaOrder] forKey:NOTIFICATIONS_KEY];
        [userDefaults synchronize];
        
        [userDefaults setObject:mfaOrder forKey:Mfa_ORDER_KEY];
        [userDefaults synchronize];
    }
}

- (NSMutableArray<NSDictionary *> *)getSavedMfas {
    NSMutableDictionary* mfasDict = [self getMfasDict];
    NSMutableArray* mfaOrder =  [self getMfasOrder];
    NSMutableArray <NSDictionary *> *savedMfas = [[NSMutableArray alloc] init];
    for (id value in mfaOrder) {
        [savedMfas addObject:[mfasDict objectForKey:value]];
    }
    return savedMfas;
}

- (NSMutableArray <NSDictionary *> *) getPendingMfas {
    NSMutableDictionary* mfasDict = [self getMfasDict];
    NSMutableArray <NSDictionary *> *pendingMfas = [[NSMutableArray alloc] init];
    NSMutableArray <NSString *> *requestIdsToDismiss = [[NSMutableArray alloc] init];
    
    [mfasDict enumerateKeysAndObjectsUsingBlock:^(id key, NSDictionary * value, BOOL* stop) {
        if ([self isMfaValid:value]) {
            [pendingMfas addObject:value];
        } else {
            [requestIdsToDismiss addObject:[value objectForKey:REQUEST_ID_KEY]];
        }
    }];
    
    [self dismissNotificaitons:requestIdsToDismiss];
    
    return pendingMfas;
}

- (void) clearAll {
    [userDefaults removeObjectForKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
}

@end
