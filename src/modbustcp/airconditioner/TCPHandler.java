package modbustcp.airconditioner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import utils.HexUtils;

public class TCPHandler implements Runnable {
	private final SelectionKey sk;
	private final SocketChannel sc;
	private final String listenIp;

	int state;

	public TCPHandler(SelectionKey sk, SocketChannel sc, String listenIp) {
		this.sk = sk;
		this.sc = sc;
		this.listenIp = listenIp;
		state = 0; // ��ʼ״̬����ΪReading
	}

	@Override
	public void run() {
		try {
			if (state == 0) {
				read();
			}/* else {
				send(); // ��������
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private synchronized void read() throws IOException {
		// non-blocking�²�����Readers����ΪReaders��֧��non-blocking
		byte[] arr = new byte[1024];
		ByteBuffer buf = ByteBuffer.wrap(arr);

		int numBytes = sc.read(buf); // ��ȡ�ַ���
		if (numBytes == -1) {
			System.out.println("[Warning!] A client has been closed.");
			closeChannel();
			return;
		}
		if (arr != null && arr.length > 0) {
			byte[] retBuf = dipatcher(arr); // �߼�����
			state = 1; // �ı�״̬
			sk.interestOps(SelectionKey.OP_WRITE); // ͨ��key�ı�ͨ����ע���¼�
			sk.selector().wakeup(); // ʹһ����selector.select()����������ס��selector������������
			
			send(retBuf);
		}
	}
	
	private void send(byte[] data) throws IOException {
		data = this.correctData(data);
		String dataString = new String(data)+"\r\n";
		ByteBuffer buf = ByteBuffer.wrap(dataString.getBytes()); // wrap�Զ���buf��position��Ϊ0, ���Բ���Ҫ��flip()

		while (buf.hasRemaining()) {
			sc.write(buf); // �ش���client��Ӧ���ַ���, ����buf��positionλ�õ�limitλ��Ϊֹ֮�������
		}

		buf.compact();
		state = 0; // �ı�״̬
		sk.interestOps(SelectionKey.OP_READ); // ͨ��key�ı�ͨ����ע���¼�
		sk.selector().wakeup(); // ʹһ������ס��selector������������
	}

	/**
	 * 
	 * @Description: ת�� 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param arr
	 * @return
	 */
	public byte[] dipatcher(byte[] arr) {
		Socket client = null;
		OutputStream out = null;
		ByteArrayOutputStream outStream = null;
		InputStream in = null;
		try {
			client = new Socket(this.listenIp, 502); // �B����Ŀ�ĵ�  
            System.out.println("Connected to "+this.listenIp);  
            client.setSoTimeout(3000);
            out = client.getOutputStream();
            
            in = client.getInputStream();
            
            out.write(arr); // ת�������Կͻ��˵�����  
            out.flush(); // ǿ�ƽ����������������
            
            outStream = new ByteArrayOutputStream();
			int blockLen = 0;
			while (blockLen == 0) {
				blockLen = in.available();
			}
            byte[] buf = new byte[blockLen];
            if (in.read(buf) != -1) {
				System.out.print(HexUtils.binaryToHexString(buf));
            	outStream.write(buf);
            }
            System.out.println();
            outStream.close();
            in.close();
            out.close();
            client.close();

            return outStream.toByteArray();
		} catch (IOException e) {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (client != null) {
					client.close();
				}

				if (outStream != null) {
					e.printStackTrace();
					return outStream.toByteArray();
				} else {
					return null;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return null;
	}
	/**
	 * 
	 * @Description:������5,��6���ֽ��������ĳ������� 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param arr
	 * @return
	 */
	private byte[] correctData(byte[] arr) {
		if (arr != null && arr.length >= 6) {
			short dataLength = (short)(arr.length - 6);
			arr[4] = (byte)((dataLength & 0xFF00)>>8);
			arr[5] = (byte)(dataLength & 0x00FF);
		}
		return arr;
	}
	
	private void closeChannel() {  
        try {  
            sk.cancel();  
            sc.close();  
        } catch (IOException e1) {  
            e1.printStackTrace();  
        }  
    }
}
