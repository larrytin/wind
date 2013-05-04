#import "com/goodow/realtime/Document.h"
@class GDRCollaboratorJoinedEvent;
@class GDRCollaboratorLeftEvent;
@class GDRDocumentSaveStateChangedEvent;
typedef void (^GDRCollaboratorJoinedBlock)(GDRCollaboratorJoinedEvent * event);
typedef void (^GDRCollaboratorLeftBlock)(GDRCollaboratorLeftEvent * event);
typedef void (^GDRDocumentSaveStateChangedBlock)(GDRDocumentSaveStateChangedEvent * event);

@interface GDRDocument (OCNI)
-(void)addCollaboratorJoinedListener:(GDRCollaboratorJoinedBlock)handler;
-(void)addCollaboratorLeftListener:(GDRCollaboratorLeftBlock)handler;
-(void)addDocumentSaveStateListener:(GDRDocumentSaveStateChangedBlock)handler;
-(void)removeCollaboratorJoinedListener:(GDRCollaboratorJoinedBlock)handler;
-(void)removeCollaboratorLeftListener:(GDRCollaboratorLeftBlock)handler;

@end
