#import "com/goodow/realtime/CollaborativeMap.h"

@interface GDRCollaborativeMap (OCNI)
@property(readonly) int size;

-(void)addValueChangedListener:(GDRValueChangedBlock)handler;
-(void)removeValueChangedListener:(GDRValueChangedBlock)handler;
@end
