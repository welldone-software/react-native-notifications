#import <Foundation/Foundation.h>
#import "RNNotificationCenter.h"

@import UserNotifications;

@interface RNNotificationsStorage : NSObject

- (void) saveMFA:(NSDictionary *) mfa;
- (void) updateMFA:(NSDictionary *) mfa answer:(BOOL *) answer;
- (void) saveFetchedMFAs:(NSArray <NSDictionary *> *) fetchedMFAs;
- (NSMutableArray <NSDictionary *> *) getPendingMFAs;
- (BOOL *) isMfaAnswered:(NSString *) requestId;
- (void) clearAll;

@end
