#import "com/goodow/realtime/CollaborativeString.h"
#import "GDRCollaborativeObject+OCNI.h"

@class GDRTextDeletedEvent;
@class GDRTextInsertedEvent;
typedef void (^GDRTextDeletedBlock)(GDRTextDeletedEvent * event);
typedef void (^GDRTextInsertedBlock)(GDRTextInsertedEvent * event);

@interface GDRCollaborativeString (OCNI)
@property(readonly) int length;

-(void)addTextDeletedListener:(GDRTextDeletedBlock)handler;
-(void)addTextInsertedListener:(GDRTextInsertedBlock)handler;
-(void)removeStringListener:(GDREventHandlerBlock)handler;

@end
