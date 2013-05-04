#import "GDRCollaborativeList+OCNI.h"
#import "GDRealtime.h"

@implementation GDRCollaborativeList (OCNI)
@dynamic length;

-(void)addValuesAddedListener:(GDRValuesAddedBlock)handler{
  [self addEventListener:[GDREventTypeEnum VALUES_ADDED] handler:handler opt_capture:NO];
}
-(void)addValuesRemovedListener:(GDRValuesRemovedBlock)handler{
  [self addEventListener:[GDREventTypeEnum VALUES_REMOVED] handler:handler opt_capture:NO];
}
-(void)addValuesSetListener:(GDRValuesSetBlock)handler{
  [self addEventListener:[GDREventTypeEnum VALUES_SET] handler:handler opt_capture:NO];
}
-(void)removeListListener:(GDREventHandlerBlock)handler{
  [self removeEventListener:[GDREventTypeEnum VALUES_ADDED] handler:handler opt_capture:NO];
  [self removeEventListener:[GDREventTypeEnum VALUES_REMOVED] handler:handler opt_capture:NO];
  [self removeEventListener:[GDREventTypeEnum VALUES_SET] handler:handler opt_capture:NO];
}
@end
