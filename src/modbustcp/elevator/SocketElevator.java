package modbustcp.elevator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import utils.HexUtils;

public class SocketElevator {
	private static final int BUF_SIZE = 16;
	private static final String MODBUS_TCP_IP = "192.168.218.15";
	private static final int MODBUS_TCP_PORT = 502;
	private static final int TIMEOUT = 1;// 1秒
	
	// 客户端线程:每秒实时采集一次电梯数据
	public class Monitor implements Runnable {
		@Override
		public void run() {
			
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		SocketChannel socketChannel = null;
		Selector selector = null;
		
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			// 1. 向服务器发起连接请求
			boolean isConnected = socketChannel.connect(new InetSocketAddress(MODBUS_TCP_IP, MODBUS_TCP_PORT));
			System.out.println(isConnected?"连接成功":"正在连接。。。");
			
			// 1.1 检查socketChannel是否已打开
			System.out.println(socketChannel.isOpen()?"已打开":"未打开");
			
			
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
			
			// 2. 等待连接
			while (!socketChannel.finishConnect()) {
				System.out.println("正在连接");
			}
			
			// 3. 边发送边接收数据
			while (true) {
				// selector.select();	// 阻塞，直到其中至少有一个IO事件就绪
				// 设置TIMEOUT秒阻塞，如果超时未有就绪IO，则继续轮询
				if (selector.select(TIMEOUT) == 0) {
					System.out.println("切换channel");
					TimeUnit.SECONDS.sleep(1);
					continue;
				}

				// 4. 根据就绪IO事件派发到对应处理方法
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = (SelectionKey) iter.next();
					/*iter.remove();*/
					
					if (key.isConnectable()) {
						
					}
					/*if (key.isAcceptable()) {
						if(socketChannel.isConnectionPending()){
	                        if(socketChannel.finishConnect()){
	                            //只有当连接成功后才能注册OP_READ事件
	                            key.interestOps(SelectionKey.OP_READ);
	
	                			// 请求数据
	                			byte[] sendInfo = new byte[]{ 0x00,0x07, 0x00,0x00, 0x00,0x06, 0x01, 0x03, 0x00,0x00, 0x00,0x01 };
	                			ByteBuffer writeBuffer = ByteBuffer.wrap(sendInfo);
	                			socketChannel.write(writeBuffer);
	                        }
	                        else{
	                            key.cancel();
	                        }
	                    }    
					}*/
					if (key.isReadable()) {
						handleRead(key);
					}
					if (key.isWritable() && key.isValid()) {
						handleWrite(key);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socketChannel != null) {
					socketChannel.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @Description:处理读IO就绪事件 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
	 *
	 * @param 
	 * @param key
	 * @throws IOException
	 */
	private static void handleRead(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel)key.channel();// 与原来的channel是同个实例
		ByteBuffer readBuffer = ByteBuffer.allocate(BUF_SIZE);

		while (sc.read(readBuffer) > 0) {
			readBuffer.flip();
			System.out.print(HexUtils.binaryToHexString(readBuffer.array()));
			readBuffer.clear();
		}
		System.out.println();
	}


	
	
	/**
	 * 
	 * @Description: 处理写IO就绪事件 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
	 *
	 * @param 
	 * @param key
	 * @throws IOException
	 */
	private static void handleWrite(SelectionKey key) throws IOException {
		//byte[] sendInfo = new byte[]{ 0x00,0x07, 0x00,0x00, 0x00,0x06, 0x01, 0x03, 0x00,0x06, 0x00,0x01 };
		byte[] sendInfo = new byte[] { 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x01, 0x03, 0x00, 0x06, 0x00, 0x03 };
		SocketChannel channel = (SocketChannel)key.channel();	// 从写IO事件就绪的key中获取通道
		ByteBuffer sendBuffer = ByteBuffer.wrap(sendInfo);	// 数据写入缓冲区
		channel.write(sendBuffer);
		System.out.println("已发送数据：" + new String(sendInfo));
	}
	
	// [0, 7, 0, 0, 0, 9, 1, 3, 6, 2, 16, 15, 18, 39, 1, 0, 0, 0, 0, 0]
}
