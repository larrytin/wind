#import "com/goodow/realtime/IndexReference.h"
#import "GDRealtime.h"

@interface GDRIndexReference (ObjC)
@property(readonly) BOOL canBeDeleted;
@property(readonly) int index;
@property(readonly) GDRCollaborativeObject * referencedObject;
@end
