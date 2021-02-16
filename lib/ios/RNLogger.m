#import <Foundation/Foundation.h>
#import "RNLogger.h"

@implementation RNLogger : NSObject

-(void) saveLog: (NSString *)type tag:(NSString *)tag message:(NSString *)message {
    NSString *filepath = [self getPathForDirectory];
    NSString *log = [self parseLog:type tag:tag message:message];
    
    NSData *data = [log dataUsingEncoding:NSUTF8StringEncoding];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if (![fileManager fileExistsAtPath:filepath]) {
        BOOL success = [[NSFileManager defaultManager] createFileAtPath:filepath contents:data attributes:nil];
        if (!success) {
            NSLog(@"[RNLogger] failed to create logs file");
        }
        return;
    }
    
    @try {
        NSFileHandle *fileHandle = [NSFileHandle fileHandleForUpdatingAtPath:filepath];
        [fileHandle seekToEndOfFile];
        [fileHandle writeData:data];
        return;
    } @catch (NSException *exception) {
        NSMutableDictionary * info = [NSMutableDictionary dictionary];
        [info setValue:exception.name forKey:@"ExceptionName"];
        [info setValue:exception.reason forKey:@"ExceptionReason"];
        [info setValue:exception.callStackReturnAddresses forKey:@"ExceptionCallStackReturnAddresses"];
        [info setValue:exception.callStackSymbols forKey:@"ExceptionCallStackSymbols"];
        [info setValue:exception.userInfo forKey:@"ExceptionUserInfo"];
        NSError *err = [NSError errorWithDomain:@"RNLogger" code:0 userInfo:info];
        NSLog(@"[RNLogger] %@", err);
    }
}

- (NSString *)getPathForDirectory {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    return [NSString stringWithFormat:@"%@/silverfort/logs/notifications_logs.txt", [paths firstObject]];
}

- (NSString *)parseLog: (NSString *)type tag:(NSString *)tag message:(NSString *)message {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"MMMM DD YYYY, h:mm:ss a"];
    NSDate *currentDate = [NSDate date];
    NSString *dateString = [formatter stringFromDate:currentDate];
    return [NSString stringWithFormat:@"[%@] [%@] %@: %@\n", type, dateString, tag, message];
}

- (NSString *)parseDictionaryToJSON: (NSDictionary *)object {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:object options:NSJSONWritingPrettyPrinted error:&error];
    if (! jsonData) {
        return nil;
    } else {
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        return [NSString stringWithFormat:@"MFA arrived in background: %@", jsonString];
    }
}

@end
