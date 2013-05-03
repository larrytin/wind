#import "com/goodow/realtime/CollaborativeString.h"
#import "GDRealtime.h"

@interface GDRCollaborativeString (ObjC)
@property(readonly) int length;

//-(void)addTextDeletedListener:(void(^)(GDRTextDeletedEvent * event))handler;
//-(void)addTextInsertedListener:(void(^)(GDRTextInsertedEvent * event))handler;

@end
