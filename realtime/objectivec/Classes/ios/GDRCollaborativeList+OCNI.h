#import "com/goodow/realtime/CollaborativeList.h"
#import "GDRCollaborativeObject+OCNI.h"
@class GDRValuesAddedEvent;
@class GDRValuesRemovedEvent;
@class GDRValuesSetEvent;
typedef void (^GDRValuesAddedBlock)(GDRValuesAddedEvent * event);
typedef void (^GDRValuesRemovedBlock)(GDRValuesRemovedEvent * event);
typedef void (^GDRValuesSetBlock)(GDRValuesSetEvent * event);

@interface GDRCollaborativeList (OCNI)
@property int length;

-(void)addValuesAddedListener:(GDRValuesAddedBlock)handler;
-(void)addValuesRemovedListener:(GDRValuesRemovedBlock)handler;
-(void)addValuesSetListener:(GDRValuesSetBlock)handler;
-(void)removeListListener:(GDREventHandlerBlock)handler;
@end
