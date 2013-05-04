#import "com/goodow/realtime/Model.h"
@class GDRUndoRedoStateChangedEvent;
typedef void (^GDRUndoRedoStateChangedBlock)(GDRUndoRedoStateChangedEvent * event);

@interface GDRModel (OCNI)
@property(readonly) BOOL canRedo;
@property(readonly) BOOL canUndo;
@property(readonly) BOOL isReadOnly;

-(void)addUndoRedoStateChangedListener:(GDRUndoRedoStateChangedBlock)handler;
@end
