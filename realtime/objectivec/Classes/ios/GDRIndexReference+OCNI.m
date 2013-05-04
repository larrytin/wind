#import "GDRIndexReference+OCNI.h"
#import "GDRealtime.h"

@implementation GDRIndexReference (OCNI)
@dynamic canBeDeleted, index, referencedObject;

-(void)addReferenceShiftedListener:(GDRReferenceShiftedBlock)handler{
  [self addReferenceShiftedListenerWithGDREventHandler:handler];
}
-(void)removeReferenceShiftedListener:(GDRReferenceShiftedBlock)handler{
  [self removeReferenceShiftedListenerWithGDREventHandler:handler];
}
@end
