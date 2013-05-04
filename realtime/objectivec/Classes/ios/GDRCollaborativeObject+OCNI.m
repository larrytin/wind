#import "GDRCollaborativeObject+OCNI.h"
#import "GDRealtime.h"
@implementation GDRCollaborativeObject (OCNI)

-(void)addEventListener:(GDREventTypeEnum *)type handler:(GDREventHandlerBlock)handler opt_capture:(BOOL)opt_capture{

}
-(void)addObjectChangedListener:(GDRObjectChangedBlock)handler{
  [self addEventListener:[GDREventTypeEnum OBJECT_CHANGED] handler:handler opt_capture:NO];
}
-(void)removeEventListener:(GDREventTypeEnum *)type handler:(GDREventHandlerBlock)handler opt_capture:(BOOL)opt_capture{
  [self removeEventListenerWithGDREventTypeEnum:type withGDREventHandler:handler withBOOL:opt_capture];
  
}
-(void)removeObjectChangedListener:(GDRObjectChangedBlock)handler{
  [self removeEventListener:[GDREventTypeEnum OBJECT_CHANGED] handler:handler opt_capture:NO];
}
@end
