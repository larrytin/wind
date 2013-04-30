#import "com/goodow/realtime/IndexReference.h"
#import "GDRealtime.h"

@interface GDRIndexReference (ObjC)
@property(readonly, getter = __canBeDeleted) BOOL canBeDeleted;
@property(readonly, getter = __index) int index;
@property(readonly, getter = __referencedObject) GDRCollaborativeObject * referencedObject;
@end
