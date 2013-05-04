#import "GDRCollaborativeMap+OCNI.h"
#import "GDRealtime.h"

@implementation GDRCollaborativeMap (OCNI)
@dynamic size;

-(void)addValueChangedListener:(GDRValueChangedBlock)handler{
  [self addEventListener:[GDREventTypeEnum VALUE_CHANGED] handler:handler opt_capture:NO];
}
-(void)removeValueChangedListener:(GDRValueChangedBlock)handler{
  [self removeEventListener:[GDREventTypeEnum VALUE_CHANGED] handler:handler opt_capture:NO];
}
@end
