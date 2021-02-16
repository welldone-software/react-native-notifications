#import <Foundation/Foundation.h>

@interface RNLogger : NSObject

- (void) saveLog: (NSString *)type tag:(NSString *)tag message:(NSString *)message;
- (NSString *) parseDictionaryToJSON: (NSDictionary *)object;

@end
