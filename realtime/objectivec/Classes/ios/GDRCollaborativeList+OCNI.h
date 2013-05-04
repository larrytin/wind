#import "com/goodow/realtime/CollaborativeList.h"

@interface GDRCollaborativeList (OCNI)
@property int length;

-(void)addValuesAddedListener:(GDRValuesAddedBlock)handler;
-(void)addValuesRemovedListener:(GDRValuesRemovedBlock)handler;
-(void)addValuesSetListener:(GDRValuesSetBlock)handler;
-(void)removeListListener:(GDREventBlock)handler;
@end
