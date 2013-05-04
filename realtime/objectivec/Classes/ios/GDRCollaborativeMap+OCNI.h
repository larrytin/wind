#import "com/goodow/realtime/CollaborativeMap.h"
@class GDRValueChangedEvent;
typedef void (^GDRValueChangedBlock)(GDRValueChangedEvent * event);

@interface GDRCollaborativeMap (OCNI)
@property(readonly) int size;

-(void)addValueChangedListener:(GDRValueChangedBlock)handler;
-(void)removeValueChangedListener:(GDRValueChangedBlock)handler;
@end
