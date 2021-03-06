#import "RNNotificationEventHandler.h"
#import "RNEventEmitter.h"
#import "RNNotificationUtils.h"
#import "RCTConvert+RNNotifications.h"
#import "RNNotificationParser.h"
#import "RNNotificationsStore.h"

@implementation RNNotificationEventHandler {
    RNNotificationsStore* _store;
}

- (instancetype)initWithStore:(RNNotificationsStore *)store {
    self = [super init];
    _store = store;
    return self;
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSDictionary *)tokens {
    NSString *fcmTokenRepresentation = [[tokens valueForKey:@"fcm"] isKindOfClass:[NSString class]] ? [tokens valueForKey:@"fcm"] : [RNNotificationUtils deviceTokenToString:[tokens valueForKey:@"fcm"]];
    NSString *apnsTokenRepresentation = [[tokens valueForKey:@"apns"] isKindOfClass:[NSString class]] ? [tokens valueForKey:@"apns"] : [RNNotificationUtils deviceTokenToString:[tokens valueForKey:@"apns"]];
    [RNEventEmitter sendEvent:RNRegistered body:@{@"fcmToken": fcmTokenRepresentation, @"apnsToken": apnsTokenRepresentation}];
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [RNEventEmitter sendEvent:RNRegistrationFailed body:@{@"code": [NSNumber numberWithInteger:error.code], @"domain": error.domain, @"localizedDescription": error.localizedDescription}];
}

- (void)didReceiveForegroundNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    [_store setPresentationCompletionHandler:completionHandler withCompletionKey:notification.request.identifier];
    [RNEventEmitter sendEvent:RNNotificationReceived body:[RNNotificationParser parseNotification:notification]];
}

- (void)didReceiveNotificationResponse:(UNNotificationResponse *)response completionHandler:(void (^)(void))completionHandler {
    [_store setActionCompletionHandler:completionHandler withCompletionKey:response.notification.request.identifier];
    NSDictionary* responseDict = [RNNotificationParser parseNotificationResponse:response];
    [[RNNotificationsStore sharedInstance] setInitialAction:responseDict];
    [RNEventEmitter sendEvent:RNNotificationOpened body:responseDict];
}

@end
