#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'qrscan'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter project.'
  s.description      = <<-DESC
A new Flutter project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.resource_bundles = {
    'qrscan' => ['Resources/**/*.{png}']
  }
  
  s.dependency 'Flutter'
  s.dependency 'ATBarSDK'
  
  s.frameworks = "AVFoundation", "CoreMedia", "CoreVideo", "QuartzCore"
  s.libraries = "iconv"
  
  s.swift_version = '5.0'
#  s.static_framework = false
  s.ios.deployment_target = '8.0'

end

