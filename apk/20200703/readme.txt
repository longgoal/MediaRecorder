mediarecorder-20200703.apk
1,默认保存到外置t卡，如果没有t卡，保存到内置存储中。
2,视频分辨率1280*720
3，如果没有认为干预，最多录像20个小时就停止了。

代码修改记录

1，为了直接能访问外置t卡，做了如下修改。
--平台apk签名
   >>manifest文件加入 android:sharedUserId="android.uid.system"
     上传时，暂时没有加如到manifest中，需要加入的时候local 代码去修改。
     
   >>把下面这些文件和待签名的apk(例如app-debug.apk)复制到一个目录下
   out/host/linux-x86/lib64/libconscrypt_openjdk_jni.so  
   build/make/target/product/security/platform.pk8  
   build/make/target/product/security/platform.x509.pem  
   out/host/linux-x86/framework/signapk.jar
   
   然后在aosp编译环境下执行
   java -Djava.library.path=. -jar signapk.jar platform.x509.pem platform.pk8 app-debug.apk app-debug-s.apk
   
   然后adb 安装
   app-debug-s.apk   

