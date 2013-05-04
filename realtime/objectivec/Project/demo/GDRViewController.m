//
//  GDRViewController.m
//  demo
//
//  Created by dev on 13-4-27.
//  Copyright (c) 2013年 Larry Tin. All rights reserved.
//

#import "GDRViewController.h"
#import "GDRealtime.h"

@interface GDRViewController ()

@end

@implementation GDRViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
  [GDRRealtime load:@"" onLoaded:^(GDRDocument *document) {
    GDRModel *mod = document.getModel;
    GDRCollaborativeString * str = [mod createString:@"abc"];
    [str addTextInsertedListener:^(GDRTextInsertedEvent *event) {
      NSLog(@"abc888");
    }];
    [str append:@"de"];
    NSLog(@"yyy");
  } initializer:nil error:nil];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
