#import "GDRCollaborativeList+OCNI.h"
#import "GDRealtime.h"

@implementation GDRCollaborativeList (OCNI)
@dynamic length;

-(void)addValuesAddedListener:(GDRValuesAddedBlock)handler{
  [self addValuesAddedListenerWithGDREventHandler:handler];
}
-(void)addValuesRemovedListener:(GDRValuesRemovedBlock)handler{
  [self addValuesRemovedListenerWithGDREventHandler:handler];
}
-(void)addValuesSetListener:(GDRValuesSetBlock)handler{
  [self addValuesSetListenerWithGDREventHandler:handler];
}
-(void)removeListListener:(GDREventBlock)handler{
  [self removeListListenerWithGDREventHandler:handler];
}
@end
