# myBluetooth
手机蓝牙串口的调试助手demo实现 手机蓝牙串口的调试助手demo实现源码，另附有打包好的APK文件。

因为有人反映CSDN积分太贵，我决定开源出来，共给大家学习！

在Android蓝牙通信连接的开发中，需要在AndroidManifest.xml配置文件中增加相应的权限，需要添加BLUETOOTH和BLUETOOTH_ADMIN这两个权限。在确保手机支持蓝牙的前提下，应用程序通过获取BluetoothAdapter类，根据其getDefaultAdapter()方法获取该类的实例，通过.isEnabled()查询手机蓝牙是否为打开状态。通过BluetoothAdapter.getBondedDevices()获取该手机蓝牙设备已经匹配的蓝牙设备信息；搜索新设备可通过使用BluetoothAdapter.startDiscovery()方法进入一个为时12秒的扫描发现外围蓝牙其他设备，当发系统现设备时，随即系统发出广播，程序通过注册并接受该广播，获取该广播信息里的蓝牙设备BluetoothDevice实例，从而获取该蓝牙设备的信息。

蓝牙通信技术可以在短距离传输中以无线电的方式传递数据，借助蓝牙通信技术来创造便利性，智能性和可控性[8]。实现Android应用程序与蓝牙设备连接，类似TCP连接，构建Android蓝牙通信连接两个关键类是BluetoothSocket和BluetoothServerSocket[9]。

在Android蓝牙通信连接的开发中，实现基本功能蓝牙通信软件，则需要在软件实现服务端和客户端两种机制[10]。这种通信构建需要在服务端程序根据UUID(通用唯一识别码)调用listenUsingRfcommWithServiceRecord(String,UUID）获得BluetoothServerSocket对象，在子线程调用accept()方法开启监听线程。在客户端也根据UUID(通用唯一识别码)调用createRfcommSocketToServiceRecord(UUID)获取BluetoothSocket，在子线程调用connect()方法发起请求连接。

构建蓝牙通信连接后，不管是服务器还是客户端机制，彼此都会连接好的BluetoothSocket对象，获取对应的输入字节流（inputstream）和输出字节流（outputstream），可调用它们的read(byte[])和write(byte[])方法来分别实现对数据流的读和写，实现蓝牙数据通信功能。




————————————————
版权声明：本文为CSDN博主「搂梦123」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。


我的博客：https://blog.csdn.net/sinat_27064327/article/details/80527460

欢迎大家留言，以后我会把博客用起来，多开源项目出来一起学习
