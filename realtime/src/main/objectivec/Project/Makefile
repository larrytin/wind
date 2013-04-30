.SUFFIXES: .java .m
.PHONY: default clean

# Change to where distribution was unzipped.
J2OBJC_DIST = /Users/retechretech/dev/tools/lib/j2objc/dist

ifdef CONFIGURATION_BUILD_DIR
# In Xcode build
BUILD_DIR = $(CONFIGURATION_BUILD_DIR)/build
ARCHFLAGS = $(ARCHS:%=-arch %)
SDKFLAGS = -isysroot $(SDKROOT)
else
# In command-line build
BUILD_DIR = $(HOME)/build
ARCHFLAGS =
SDKFLAGS =
endif

ifdef OPTIMIZATION_LEVEL
DEBUGFLAGS := $(DEBUGFLAGS) -O$(OPTIMIZATION_LEVEL)
endif

ifdef OTHER_CFLAGS
DEBUGFLAGS := $(DEBUGFLAGS) $(OTHER_CFLAGS)
endif

# Workaround for iPhoneSimulator SDK's gcc bug
ifdef EFFECTIVE_PLATFORM_NAME
ifneq ($(EFFECTIVE_PLATFORM_NAME), -iphonesimulator)
WARNINGS := $(WARNINGS) -Wreturn-type
endif
endif

J2OBJCC_FLAGS = -ObjC $(WARNINGS) $(SDKFLAGS) $(ARCHFLAGS) $(DEBUGFLAGS)
J2OBJCC = $(J2OBJC_DIST)/j2objcc $(J2OBJCC_FLAGS)

#CONFIGURATION_BUILD_DIR = /Users/retechretech/Library/Developer/Xcode/DerivedData/GDRealtime-dqlyespfgoikflalqsduflxaknex/Build/Products/Debug-iphonesimulator
#CONFIGURATION_TEMP_DIR = /Users/retechretech/Library/Developer/Xcode/DerivedData/GDRealtime-dqlyespfgoikflalqsduflxaknex/Build/Intermediates/GDRealtime.build/Debug-iphonesimulator
pods_obj_dir = $(subst GDRealtime.build,Pods.build,$(CONFIGURATION_TEMP_DIR)/Pods-test.build/Objects-normal/$(ARCHS))
JAVA_GEN_DIR = ../../../main/generated_objectivec

TEST_SRC_DIR = ../../../test/java
TEST_GEN_DIR = ../../../test/generated_objectivec
TEST_BUILD_DIR = ../../../../target/test-objectivec
TEST_SOURCES = $(shell find $(TEST_SRC_DIR) -name *.java)
TEST_SOURCE_LIST = $(TEST_BUILD_DIR)/test.sources.list
TEST_TEMP_SOURCES = $(subst $(TEST_SRC_DIR), $(TEST_GEN_DIR), $(TEST_SOURCES))
TEST_GEN_SOURCES = $(TEST_TEMP_SOURCES:.java=.m)
TEST_TEMP_OBJCS = $(subst $(TEST_SRC_DIR), $(TEST_BUILD_DIR), $(TEST_SOURCES))
TEST_OBJCS = $(TEST_TEMP_OBJCS:.java=.o)
TEST_BINS = $(TEST_OBJCS:.o=)

default: test
	
$(TEST_BUILD_DIR)/%.o: $(TEST_GEN_DIR)/%.m $(TEST_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ \
	  -g -I$(JAVA_GEN_DIR) -I$(TEST_GEN_DIR) \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses

$(TEST_BUILD_DIR)/%: $(TEST_BUILD_DIR)/%.o $(JAVA_LIB)
	@$(J2OBJCC) $< -o $@ \
	  -g -Werror \
	  -ljunit -ltest -L$(CONFIGURATION_BUILD_DIR)

link-test: $(TEST_GEN_DIR) $(TEST_OBJCS) $(TEST_BINS)

test: link-test $(TEST_BINS)
	@/bin/sh runtests.sh $(TEST_BINS)

$(TEST_GEN_DIR):
	@mkdir -p $(TEST_GEN_DIR)
$(TEST_BUILD_DIR):
	@mkdir -p $(TEST_BUILD_DIR)
	
clean:
	@rm -rf $(TEST_BUILD_DIR)