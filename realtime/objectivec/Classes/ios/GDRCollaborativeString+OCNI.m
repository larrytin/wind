#import "GDRCollaborativeString+OCNI.h"
#import "GDRealtime.h"

@implementation GDRCollaborativeString (OCNI)
@dynamic length;

-(void)addTextDeletedListener:(GDRTextDeletedBlock)handler{
  [self addEventListener:[GDREventTypeEnum TEXT_DELETED] handler:handler opt_capture:NO];
}
-(void)addTextInsertedListener:(GDRTextInsertedBlock)handler{
  [self addEventListener:[GDREventTypeEnum TEXT_INSERTED] handler:handler opt_capture:NO];
}
-(void)removeStringListener:(GDREventHandlerBlock)handler{
  [self removeEventListener:[GDREventTypeEnum TEXT_DELETED] handler:handler opt_capture:NO];
  [self removeEventListener:[GDREventTypeEnum TEXT_INSERTED] handler:handler opt_capture:NO];
}
@end
