#import "com/goodow/realtime/Model.h"

@interface GDRModel (ObjC)
@property(readonly, getter = __canRedo) BOOL canRedo;
@property(readonly, getter = __canUndo) BOOL canUndo;
@property(readonly, getter = __isReadOnly) BOOL isReadOnly;
@end
