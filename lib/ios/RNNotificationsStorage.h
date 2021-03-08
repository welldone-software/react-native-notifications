#import <Foundation/Foundation.h>
#import "RNNotificationCenter.h"

@import UserNotifications;

@interface RNNotificationsStorage : NSObject

- (void) saveMFA:(NSDictionary *) mfa;
- (void) updateMFA:(NSString *) requestId answer:(BOOL *) answer;
- (void) saveFetchedMFAs:(NSArray <NSDictionary *> *) fetchedMFAs;
- (NSMutableArray <NSDictionary *> *) getPendingMFAs;
- (void) clearAll;

@end
