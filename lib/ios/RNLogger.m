#import <Foundation/Foundation.h>
#import "RNLogger.h"

@implementation RNLogger : NSObject

NSString* BASE_DIR = @"%@/silverfort/logs/%@";
NSString* LOGS_FILE_1 = @"notifications_logs.txt";
NSString* LOGS_FILE_2 = @"notifications_logs_2.txt";
unsigned long long FILE_LIMIT = 20 * 1024 * 1000;

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
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    NSString *filePath1 = [NSString stringWithFormat:BASE_DIR, [paths firstObject], LOGS_FILE_1];
    NSString *filePath2 = [NSString stringWithFormat:BASE_DIR, [paths firstObject], LOGS_FILE_2];
    
    BOOL isFile1Exists = [fileManager fileExistsAtPath:filePath1];
    BOOL isFile2Exists = [fileManager fileExistsAtPath:filePath2];
    
    unsigned long long isFile1OverLimit = isFile1Exists ? [[[NSFileManager defaultManager] attributesOfItemAtPath:filePath1 error:nil] fileSize] >= FILE_LIMIT : 0;
    unsigned long long isFile2OverLimit = isFile1Exists ? [[[NSFileManager defaultManager] attributesOfItemAtPath:filePath2 error:nil] fileSize] >= FILE_LIMIT : 0;
    
    double file1Date = isFile1Exists ? [[[fileManager attributesOfItemAtPath:filePath1 error:nil] fileModificationDate] timeIntervalSince1970] : 0;
    double file2Date = isFile2Exists ? [[[fileManager attributesOfItemAtPath:filePath2 error:nil] fileModificationDate] timeIntervalSince1970] : 0;
    
    if (isFile1OverLimit) {
        if (isFile2OverLimit) {
            NSString *filePathToDelete = file1Date > file2Date ? filePath2 : filePath1;
            [fileManager removeItemAtPath:filePathToDelete error:nil];
            return filePathToDelete;
        }
        return filePath2;
    }
    
    return filePath1;
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
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}

@end
