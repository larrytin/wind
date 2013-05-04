#import "GDRCollaborativeObject+OCNI.h"
#import "GDRealtime.h"
@implementation GDRCollaborativeObject (OCNI)

-(void)addObjectChangedListener:(GDRObjectChangedBlock)handler{
  [self addObjectChangedListenerWithGDREventHandler:handler];
}
-(void)removeObjectChangedListener:(GDRObjectChangedBlock)handler{
  [self removeObjectChangedListenerWithGDREventHandler:handler];
}
@end
