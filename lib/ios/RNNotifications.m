
#import <UIKit/UIKit.h>
#import <PushKit/PushKit.h>
#import "RNNotifications.h"
#import "RNNotificationCenterListener.h"
#import "RNPushKit.h"
#import "RNNotificationCenterMulticast.h"
#import "RNNotificationsStorage.h"

@implementation RNNotifications {
    RNPushKit* _pushKit;
    RNNotificationCenterListener* _notificationCenterListener;
    RNNotificationEventHandler* _notificationEventHandler;
    RNPushKitEventHandler* _pushKitEventHandler;
    RNEventEmitter* _eventEmitter;
    RNNotificationCenterMulticast* _notificationCenterMulticast;
    RNNotificationsStorage* _storage;
}

- (instancetype)init {
    self = [super init];
    _notificationEventHandler = [[RNNotificationEventHandler alloc] initWithStore:[RNNotificationsStore new]];
    _storage = [RNNotificationsStorage new];
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

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(id)deviceToken {
    [[self sharedInstance] didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
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
    if([[UIApplication sharedApplication] applicationState] != UIApplicationStateActive) {
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
