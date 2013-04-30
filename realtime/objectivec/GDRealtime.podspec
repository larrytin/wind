Pod::Spec.new do |s|
  s.name         = "GDRealtime"
  s.version      = "0.0.1"
  s.summary      = "Goodow Realtime provides collaborative objects, events, and methods for creating collaborative applications via the use of operational transforms."
  s.homepage     = "https://github.com/goodow/realtime"
  s.author       = { "Larry Tin" => "dev@goodow.com" }
  s.source       = { :git => "https://github.com/goodow/realtime.git", :tag => "v#{s.version}" }
#  s.platform     = :ios, '5.1'

  s.default_subspec = 'default'
  s.header_mappings_dir = 'Classes/generated'
#  s.preserve_paths = 'src/main/generated_objectivec/**/*.h', 'src/test/generated_objectivec/**/*.h'
  s.resources = 'Resources/**'
  s.requires_arc = true

#  s.xcconfig = { 'HEADER_SEARCH_PATHS' => \
#    '"${PODS_ROOT}/GDRealtime/src/main/generated_objectivec" "${PODS_ROOT}/GDRealtime/src/test/generated_objectivec"' }

  s.subspec 'default' do |d|
    d.dependency 'jre_emul'
#, '~> 0.7.2'
    d.dependency 'GDRealtime/core'
    d.dependency 'GDRealtime/generated'
  end

  s.subspec 'core' do |c|
    c.source_files = 'Classes/ios/**/*.{h,m}'

    c.dependency 'Google-Diff-Match-Patch', '~> 0.0.1'
  end

  s.subspec 'generated' do |gen|
    gen.source_files = 'Classes/generated/**/*.{h,m}'
    gen.requires_arc = false
  end

  s.subspec 'test' do |test|
#    test.source_files = 'Classes/test_generated/**/*.{h,m}'

    test.dependency 'GDRealtime/core'
    test.dependency 'GDRealtime/generated'

    test.xcconfig = { 'HEADER_SEARCH_PATHS' => \
      '"${PODS_ROOT}/jre_emul/dist/include" "${PODS_ROOT}/jre_emul/jre_emul/icu4c/i18n/include" "${PODS_ROOT}/jre_emul/jre_emul/icu4c/common"' }
  end

end
