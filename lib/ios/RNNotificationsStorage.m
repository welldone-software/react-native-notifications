#import <Foundation/Foundation.h>
#import "RNNotificationsStorage.h"
#import "RNNotificationParser.h"

@implementation RNNotificationsStorage

NSUserDefaults *userDefaults;
NSString *NOTIFICATIONS_KEY = @"Notifications";
NSString *ANSWER_KEY = @"answer";
NSString *EXPIRED_TIME_KEY = @"expired_time";
NSString *REQUEST_ID_KEY = @"mfa_request_id";
int MFA_SAVE_LIMIT = 256;

- (instancetype) init {
    self = [super init];
    userDefaults = [NSUserDefaults standardUserDefaults];
    return self;
}

- (NSMutableDictionary*)clearLimit:(NSMutableDictionary*) mfas {
    int overLimitCount = (int)[mfas count] - MFA_SAVE_LIMIT;
    if (overLimitCount > 0) {
        __block int deletedCount = 0;
        [mfas enumerateKeysAndObjectsUsingBlock:^(id key, id value, BOOL* stop) {
            [mfas removeObjectForKey:key];
            deletedCount = deletedCount + 1;
            if (overLimitCount <= deletedCount) {
                *stop =YES;
                return;
            }
        }];
    }
    return mfas;
}

- (void)saveMFA:(NSDictionary *)mfa{
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    if (mfasDict == nil) {
        mfasDict = [NSMutableDictionary new];
    }
    NSString* requestId = [mfa valueForKey:REQUEST_ID_KEY];
    [mfasDict setObject:mfa forKey:requestId];
    [userDefaults setObject:[self clearLimit:mfasDict] forKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
}

- (void) updateMFA:(NSString *) requestId answer:(BOOL *) answer; {
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    NSMutableDictionary* mfa = [mfasDict valueForKey:requestId];
    [mfa setValue:[NSNumber numberWithBool:*answer] forKey:ANSWER_KEY];
    [mfasDict setObject:mfa forKey:requestId];
    [userDefaults setObject:mfasDict forKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
}

- (void)saveFetchedMFAs:(NSArray<NSDictionary *> *)fetchedMFAs {
    __block BOOL hasSavedAny = NO;
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    [fetchedMFAs enumerateObjectsUsingBlock:^(NSDictionary * value, NSUInteger idx, BOOL *stop) {
        NSString *requestId = [value valueForKey:REQUEST_ID_KEY];
        if ([mfasDict objectForKey:requestId] == nil) {
            hasSavedAny = YES;
            [mfasDict setObject:value forKey:requestId];
        }
    }];
    if (hasSavedAny) {
        [userDefaults setObject:[self clearLimit:mfasDict] forKey:NOTIFICATIONS_KEY];
        [userDefaults synchronize];
    }
}

- (NSMutableArray <NSDictionary *> *) getPendingMFAs {
    NSMutableDictionary* mfasDict = [[userDefaults dictionaryForKey:NOTIFICATIONS_KEY] mutableCopy];
    NSMutableArray <NSDictionary *> *pendingMFAs = [[NSMutableArray alloc] init];
    [mfasDict enumerateKeysAndObjectsUsingBlock:^(id key, NSDictionary * value, BOOL* stop) {
        bool hasNotAnswered = [value objectForKey:ANSWER_KEY] == nil;
        NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
        int currentTs = [[NSNumber numberWithDouble: timeStamp] intValue];
        bool hasNotExpired = [[value valueForKey:EXPIRED_TIME_KEY] intValue] <= currentTs;
        if (hasNotAnswered && hasNotExpired) {
            [pendingMFAs addObject:value];
        }
    }];
    return pendingMFAs;
}

- (void) clearAll {
    [userDefaults removeObjectForKey:NOTIFICATIONS_KEY];
    [userDefaults synchronize];
}

@end
