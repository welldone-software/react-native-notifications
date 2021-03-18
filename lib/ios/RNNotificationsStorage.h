#import <Foundation/Foundation.h>
#import "RNNotificationCenter.h"

@import UserNotifications;

@interface RNNotificationsStorage : NSObject

- (void) saveMfa:(NSDictionary *) mfa;
- (void) updateMfa:(NSDictionary *) mfa answer:(BOOL *) answer;
- (void) saveFetchedMfas:(NSArray <NSDictionary *> *) fetchedMfas;
- (NSMutableArray <NSDictionary *> *) getSavedMfas;
- (NSMutableArray <NSDictionary *> *) getPendingMfas;
- (BOOL *) isMfaAnswered:(NSString *) requestId;
- (void) clearAll;

@end
