#import "com/goodow/realtime/CollaborativeObject.h"
@class GDRObjectChangedEvent;
typedef void (^GDREventHandlerBlock)(id event);
typedef void (^GDRObjectChangedBlock)(GDRObjectChangedEvent * event);

@interface GDRCollaborativeObject (OCNI)

-(void)addEventListener:(GDREventTypeEnum *)type handler:(GDREventHandlerBlock)handler opt_capture:(BOOL)opt_capture;
-(void)addObjectChangedListener:(GDRObjectChangedBlock)handler;
-(void)removeEventListener:(GDREventTypeEnum *)type handler:(GDREventHandlerBlock)handler opt_capture:(BOOL)opt_capture;
-(void)removeObjectChangedListener:(GDRObjectChangedBlock)handler;
@end
