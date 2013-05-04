#import "GDRIndexReference+OCNI.h"
#import "GDRealtime.h"

@implementation GDRIndexReference (OCNI)
@dynamic canBeDeleted, index, referencedObject;

-(void)addReferenceShiftedListener:(GDRReferenceShiftedBlock)handler{
  [self addEventListener:[GDREventTypeEnum REFERENCE_SHIFTED] handler:handler opt_capture:NO];
}
-(void)removeReferenceShiftedListener:(GDRReferenceShiftedBlock)handler{
  [self removeEventListener:[GDREventTypeEnum REFERENCE_SHIFTED] handler:handler opt_capture:NO];
}
@end
