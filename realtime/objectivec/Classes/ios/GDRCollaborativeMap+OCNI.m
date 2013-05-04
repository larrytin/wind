#import "GDRCollaborativeMap+OCNI.h"
#import "GDRealtime.h"

@implementation GDRCollaborativeMap (OCNI)
@dynamic size;

-(void)addValueChangedListener:(GDRValueChangedBlock)handler{
  [self addValueChangedListenerWithGDREventHandler:handler];
}
-(void)removeValueChangedListener:(GDRValueChangedBlock)handler{
  [self removeValueChangedListenerWithGDREventHandler:handler];
}
@end
