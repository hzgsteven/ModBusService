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
	private static final int TIMEOUT = 1;// 1��
	
	// �ͻ����߳�:ÿ��ʵʱ�ɼ�һ�ε�������
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
			// 1. �������������������
			boolean isConnected = socketChannel.connect(new InetSocketAddress(MODBUS_TCP_IP, MODBUS_TCP_PORT));
			System.out.println(isConnected?"���ӳɹ�":"�������ӡ�����");
			
			// 1.1 ���socketChannel�Ƿ��Ѵ�
			System.out.println(socketChannel.isOpen()?"�Ѵ�":"δ��");
			
			
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
			
			// 2. �ȴ�����
			while (!socketChannel.finishConnect()) {
				System.out.println("��������");
			}
			
			// 3. �߷��ͱ߽�������
			while (true) {
				// selector.select();	// ������ֱ������������һ��IO�¼�����
				// ����TIMEOUT�������������ʱδ�о���IO���������ѯ
				if (selector.select(TIMEOUT) == 0) {
					System.out.println("�л�channel");
					TimeUnit.SECONDS.sleep(1);
					continue;
				}

				// 4. ���ݾ���IO�¼��ɷ�����Ӧ������
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = (SelectionKey) iter.next();
					/*iter.remove();*/
					
					if (key.isConnectable()) {
						
					}
					/*if (key.isAcceptable()) {
						if(socketChannel.isConnectionPending()){
	                        if(socketChannel.finishConnect()){
	                            //ֻ�е����ӳɹ������ע��OP_READ�¼�
	                            key.interestOps(SelectionKey.OP_READ);
	
	                			// ��������
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
	 * @Description:�����IO�����¼� 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param key
	 * @throws IOException
	 */
	private static void handleRead(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel)key.channel();// ��ԭ����channel��ͬ��ʵ��
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
	 * @Description: ����дIO�����¼� 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param key
	 * @throws IOException
	 */
	private static void handleWrite(SelectionKey key) throws IOException {
		//byte[] sendInfo = new byte[]{ 0x00,0x07, 0x00,0x00, 0x00,0x06, 0x01, 0x03, 0x00,0x06, 0x00,0x01 };
		byte[] sendInfo = new byte[] { 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x01, 0x03, 0x00, 0x06, 0x00, 0x03 };
		SocketChannel channel = (SocketChannel)key.channel();	// ��дIO�¼�������key�л�ȡͨ��
		ByteBuffer sendBuffer = ByteBuffer.wrap(sendInfo);	// ����д�뻺����
		channel.write(sendBuffer);
		System.out.println("�ѷ������ݣ�" + new String(sendInfo));
	}
	
	// [0, 7, 0, 0, 0, 9, 1, 3, 6, 2, 16, 15, 18, 39, 1, 0, 0, 0, 0, 0]
}
