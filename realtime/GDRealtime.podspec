Pod::Spec.new do |s|
  s.name         = "GDRealtime"
  s.version      = "0.0.1"
  s.summary      = "Goodow Realtime provides collaborative objects, events, and methods for creating collaborative applications via the use of operational transforms."
  s.homepage     = "https://github.com/goodow/realtime"
  s.author       = { "Larry Tin" => "dev@goodow.com" }
  s.source       = { :git => "https://github.com/goodow/realtime.git", :tag => "v#{s.version}" }
  s.platform     = :ios, '5.1'

  s.source_files = 'src/main/objectivec/Classes/**/*.{h,m}'
  s.header_mappings_dir = 'src/main/generated_objectivec'
  s.resources = 'src/main/objectivec/Resources/**'
  s.requires_arc = true

  s.dependency 'Google-Diff-Match-Patch', '~> 0.0.1'

  s.subspec 'generated' do |g|
    g.source_files = 'src/main/generated_objectivec/**/*.{h,m}'
    g.requires_arc = false
    g.dependency 'jre_emul'
#, '~> 0.6.1'
  end
end
