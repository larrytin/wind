#import "ObjcNativeInterfaceFactory+ObjC.h"
//#import "Google-Diff-Match-Patch/DiffMatchPatch.h"

@implementation ComGoodowRealtimeUtilObjcNativeInterfaceFactory (ObjC)
-(void)setTextImpl:(GDRCollaborativeString *)str text:(NSString *)text {
//  DiffMatchPatch *dmp = [DiffMatchPatch new];
//  NSMutableArray * diffs = [dmp diff_mainOfOldString:[str getText] andNewString:text];
//  if(!diffs || [diffs count] == 0){
//    return;
//  }
//  [dmp diff_cleanupSemantic:diffs];
//  int cursor = 0;
//  for(Diff *diff in diffs){
//    switch (diff.operation) {
//      case DIFF_EQUAL:
//        cursor += [diff.text length];
//        break;
//      case DIFF_INSERT:
//        [str insertString:cursor text:text];
//        cursor += [diff.text length];
//        break;
//      case DIFF_DELETE:
//        [str removeRangeFrom:cursor to:cursor+[diff.text length]];
//        break;
//      default:
//        @throw [[JavaLangRuntimeException alloc] initWithNSString:@"Shouldn't reach here!"];
//    }
//  }
}
@end
