#import "GDRModel+OCNI.h"
#import "GDRealtime.h"

@implementation GDRModel (OCNI)
@dynamic canRedo, canUndo, isReadOnly;

-(void)addUndoRedoStateChangedListener:(GDRUndoRedoStateChangedBlock)handler{
  [self addUndoRedoStateChangedListenerWithGDREventHandler:handler];
}
@end
