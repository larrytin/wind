.SUFFIXES: .java .m
.PHONY: default clean translate link

# Change to where distribution was unzipped.
J2OBJC_DIST = ~/dev/tools/lib/j2objc/dist

SOURCE_LIST = $(JAVA_BUILD_DIR)/java.sources.list
JAVA_SRC_DIR = src/main/java
JAVA_GEN_DIR = src/main/generated_objectivec
JAVA_BUILD_DIR = target/objectivec
JAVA_SOURCES = $(shell find $(JAVA_SRC_DIR) -name *.java ! -name NativeInterface.java \
  ! -name JreNativeInterfaceFactory.java ! -name JsNativeInterfaceFactory.java \
  ! -name JsJson*.java ! -name JsMap*.java)
JAVA_TEMP_SOURCES = $(subst $(JAVA_SRC_DIR), $(JAVA_GEN_DIR), $(JAVA_SOURCES))
JAVA_GEN_SOURCES = $(JAVA_TEMP_SOURCES:.java=.m)
JAVA_TEMP_OBJCS = $(subst $(JAVA_SRC_DIR), $(JAVA_BUILD_DIR), $(JAVA_SOURCES))
JAVA_OBJCS = $(JAVA_TEMP_OBJCS:.java=.o)
JAVA_LIB = $(JAVA_BUILD_DIR)/libjava.a

JNI_SRC_DIR = src/main/objectivec/Classes/java
JNI_SOURCES = $(JNI_SRC_DIR)/com/goodow/realtime/util/NativeInterface.java
JNI_TEMP_OBJCS = $(subst $(JNI_SRC_DIR), $(JAVA_BUILD_DIR), $(JNI_SOURCES))
JNI_OBJCS = $(JNI_TEMP_OBJCS:.java=.o)
JNI_OBJC_SOURCES = $(JNI_OBJCS:.o=.placeholder)

TEST_SRC_DIR = src/test/java
TEST_GEN_DIR = src/test/generated_objectivec
TEST_BUILD_DIR = target/test-objectivec
TEST_SOURCES = $(shell find $(TEST_SRC_DIR) -name *.java)
TEST_SOURCE_LIST = $(TEST_BUILD_DIR)/test.sources.list
TEST_TEMP_SOURCES = $(subst $(TEST_SRC_DIR), $(TEST_GEN_DIR), $(TEST_SOURCES))
TEST_GEN_SOURCES = $(TEST_TEMP_SOURCES:.java=.m)
TEST_TEMP_OBJCS = $(subst $(TEST_SRC_DIR), $(TEST_BUILD_DIR), $(TEST_SOURCES))
TEST_OBJCS = $(TEST_TEMP_OBJCS:.java=.o)
TEST_BINS = $(TEST_OBJCS:.o=)

J2OBJC = $(J2OBJC_DIST)/j2objc --prefixes src/main/objectivec/package-prefixes.properties \
  --mapping src/main/objectivec/method-mappings.properties
J2OBJCC = $(J2OBJC_DIST)/j2objcc
JUNIT_JAR = $(J2OBJC_DIST)/lib/junit-4.10.jar

default: test 

pre_translate: $(JAVA_GEN_DIR)
	@rm -f $(SOURCE_LIST)
	@mkdir -p `dirname $(SOURCE_LIST)`
	@touch $(SOURCE_LIST)
        
$(JAVA_GEN_DIR)/%.m $(JAVA_GEN_DIR)/%.h: $(JAVA_SRC_DIR)/%.java
	@echo $? >> $(SOURCE_LIST)
$(JAVA_BUILD_DIR)/%.placeholder: $(JNI_SRC_DIR)/%.java
	@echo $? >> $(SOURCE_LIST)
	@mkdir -p `dirname $@`
	@touch $@

translate: pre_translate $(JAVA_GEN_SOURCES) $(JNI_OBJC_SOURCES)
	@if [ `cat $(SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(JAVA_SRC_DIR) -d $(JAVA_GEN_DIR) \
	    `cat $(SOURCE_LIST)` ; \
	fi

$(JAVA_BUILD_DIR)/%.o: $(JAVA_GEN_DIR)/%.m $(JAVA_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ -g -I$(JAVA_GEN_DIR)

$(JAVA_LIB): $(JAVA_OBJCS) $(JNI_OBJCS)
	libtool -static -o $(JAVA_LIB) $(JAVA_OBJCS) $(JNI_OBJCS)

link: translate $(JAVA_GEN_DIR) $(JAVA_LIB)

pre_translate_test: $(TEST_GEN_DIR)
	@rm -f $(TEST_SOURCE_LIST)
	@mkdir -p `dirname $(TEST_SOURCE_LIST)`
	@touch $(TEST_SOURCE_LIST)

$(TEST_GEN_DIR)/%.m $(TEST_GEN_DIR)/%.h: $(TEST_SRC_DIR)/%.java
	@echo $? >> $(TEST_SOURCE_LIST)

translate-test: pre_translate_test $(TEST_GEN_SOURCES)
	@if [ `cat $(TEST_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(JAVA_SRC_DIR):$(TEST_SRC_DIR) -d $(TEST_GEN_DIR) \
	    -classpath $(JUNIT_JAR) -Werror \
	    `cat $(TEST_SOURCE_LIST)` ; \
	fi

$(TEST_BUILD_DIR)/%.o: $(TEST_GEN_DIR)/%.m $(TEST_SRC_DIR)/%.java
	@mkdir -p `dirname $@`
	@$(J2OBJCC) -c $< -o $@ \
	  -g -I$(JAVA_GEN_DIR) -I$(TEST_GEN_DIR) \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses

$(TEST_BUILD_DIR)/%: $(TEST_BUILD_DIR)/%.o $(JAVA_LIB)
	@$(J2OBJCC) $< -o $@ \
	  -g -Werror \
	  -ljunit -ljava -L$(JAVA_BUILD_DIR)

link-test: link translate-test $(TEST_GEN_DIR) $(TEST_OBJCS) $(TEST_BINS)

test: link-test $(TEST_BINS)
	@/bin/sh runtests.sh $(TEST_BINS)

$(JAVA_GEN_DIR):
	@mkdir -p $(JAVA_GEN_DIR)
$(TEST_GEN_DIR):
	@mkdir -p $(TEST_GEN_DIR)
$(JAVA_BUILD_DIR):
	@mkdir -p $(JAVA_BUILD_DIR)
$(TEST_BUILD_DIR):
	@mkdir -p $(TEST_BUILD_DIR)

clean:
	@rm -rf $(JAVA_GEN_DIR) $(TEST_GEN_DIR) $(JAVA_BUILD_DIR) $(TEST_BUILD_DIR)
