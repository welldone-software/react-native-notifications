
#import <UIKit/UIKit.h>
#import <PushKit/PushKit.h>
#import "RNNotifications.h"
#import "RNNotificationCenterListener.h"
#import "RNPushKit.h"
#import "RNNotificationCenterMulticast.h"
#import "RNNotificationsStorage.h"
#import "RNLogger.h"

@implementation RNNotifications {
    RNPushKit* _pushKit;
    RNNotificationCenterListener* _notificationCenterListener;
    RNNotificationEventHandler* _notificationEventHandler;
    RNPushKitEventHandler* _pushKitEventHandler;
    RNEventEmitter* _eventEmitter;
    RNNotificationCenterMulticast* _notificationCenterMulticast;
    RNNotificationsStorage* _storage;
    RNLogger* _logger;
}

- (instancetype)init {
    self = [super init];
    _notificationEventHandler = [[RNNotificationEventHandler alloc] initWithStore:[RNNotificationsStore new]];
    _storage = [RNNotificationsStorage new];
    _logger = [RNLogger new];
    return self;
}

+ (instancetype)sharedInstance {
    static RNNotifications *sharedInstance = nil;
    static dispatch_once_t onceToken;
    
    dispatch_once(&onceToken, ^{
        sharedInstance = [[RNNotifications alloc] init];
    });
    return sharedInstance;
}

+ (void)startMonitorNotifications {
    [[self sharedInstance] startMonitorNotifications];
}

+ (void)startMonitorBackgroundNotifications:(NSDictionary *)payload {
    [[self sharedInstance] startMonitorBackgroundNotifications:payload];
}

+ (void)startMonitorPushKitNotifications {
    [[self sharedInstance] startMonitorPushKitNotifications];
}

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSDictionary *)tokens {
    [[self sharedInstance] didRegisterForRemoteNotificationsWithDeviceToken:tokens];
}

+ (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [[self sharedInstance] didFailToRegisterForRemoteNotificationsWithError:error];
}

+ (void)addNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [[self sharedInstance] addNativeDelegate:delegate];
}

+ (void)removeNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [[self sharedInstance] removeNativeDelegate:delegate];
}

- (void)startMonitorNotifications {
    _notificationCenterListener = [[RNNotificationCenterListener alloc] initWithNotificationEventHandler:_notificationEventHandler];
    
    _notificationCenterMulticast = [[RNNotificationCenterMulticast alloc] init];
    [[UNUserNotificationCenter currentNotificationCenter] setDelegate:_notificationCenterMulticast];
    
    [_notificationCenterMulticast addNativeDelegate:_notificationCenterListener];
}

- (void)startMonitorBackgroundNotifications:(NSDictionary *)payload {
    NSString * mfaJson = [_logger parseDictionaryToJSON:payload];
    BOOL isForeground = [[UIApplication sharedApplication] applicationState] != UIApplicationStateActive;
    NSString *stateString = isForeground ? @"Foreground" : @"Background";
    if (! mfaJson) {
        [_logger saveLog:@"ERROR" tag:@"RNNotifications" message:[NSString stringWithFormat:@"%@ MFA: Could not parse MFA", stateString]];
    } else {
        [_logger saveLog:@"LOG" tag:@"RNNotifications" message:[NSString stringWithFormat:@"%@ MFA: %@", stateString, mfaJson]];
    }
    if (!isForeground) {
        [_storage saveNotification:payload];
    }
}

- (void)startMonitorPushKitNotifications {
    _pushKitEventHandler = [RNPushKitEventHandler new];
    _pushKit = [[RNPushKit alloc] initWithEventHandler:_pushKitEventHandler];
}

- (void)didRegisterForRemoteNotificationsWithDeviceToken:(id)deviceToken {
    [_notificationEventHandler didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    [_notificationEventHandler didFailToRegisterForRemoteNotificationsWithError:error];
}

- (void)addNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [_notificationCenterMulticast addNativeDelegate:delegate];
}

- (void)removeNativeDelegate:(id<UNUserNotificationCenterDelegate>)delegate {
    [_notificationCenterMulticast removeNativeDelegate:delegate];
}

@end
