#import "com/goodow/realtime/IndexReference.h"
@class GDRCollaborativeObject;
@class GDRReferenceShiftedEvent;
typedef void (^GDRReferenceShiftedBlock)(GDRReferenceShiftedEvent * event);

@interface GDRIndexReference (OCNI)
@property(readonly) BOOL canBeDeleted;
@property(readonly) int index;
@property(readonly) GDRCollaborativeObject * referencedObject;

-(void)addReferenceShiftedListener:(GDRReferenceShiftedBlock)handler;
-(void)removeReferenceShiftedListener:(GDRReferenceShiftedBlock)handler;
@end
