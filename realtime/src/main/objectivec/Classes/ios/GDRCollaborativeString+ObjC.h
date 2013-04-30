#import "com/goodow/realtime/CollaborativeString.h"
#import "GDRealtime.h"

@interface GDRCollaborativeString (ObjC)
@property(readonly,getter = __length) int length;

//-(void)addTextDeletedListener:(void(^)(GDRTextDeletedEvent * event))handler;
//-(void)addTextInsertedListener:(void(^)(GDRTextInsertedEvent * event))handler;

@end
