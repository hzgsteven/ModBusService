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
		state = 0; // 初始状态设置为Reading
	}

	@Override
	public void run() {
		try {
			if (state == 0) {
				read();
			}/* else {
				send(); // 发送数据
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private synchronized void read() throws IOException {
		// non-blocking下不可用Readers，因为Readers不支持non-blocking
		byte[] arr = new byte[1024];
		ByteBuffer buf = ByteBuffer.wrap(arr);

		int numBytes = sc.read(buf); // 读取字符串
		if (numBytes == -1) {
			System.out.println("[Warning!] A client has been closed.");
			closeChannel();
			return;
		}
		if (arr != null && arr.length > 0) {
			byte[] retBuf = dipatcher(arr); // 逻辑处理
			state = 1; // 改变状态
			sk.interestOps(SelectionKey.OP_WRITE); // 通过key改变通道的注册事件
			sk.selector().wakeup(); // 使一个在selector.select()方法上阻塞住的selector操作立即返回
			
			send(retBuf);
		}
	}
	
	private void send(byte[] data) throws IOException {
		data = this.correctData(data);
		String dataString = new String(data)+"\r\n";
		ByteBuffer buf = ByteBuffer.wrap(dataString.getBytes()); // wrap自动把buf的position设为0, 所以不需要在flip()

		while (buf.hasRemaining()) {
			sc.write(buf); // 回传给client对应的字符串, 发送buf的position位置到limit位置为止之间的内容
		}

		buf.compact();
		state = 0; // 改变状态
		sk.interestOps(SelectionKey.OP_READ); // 通过key改变通道的注册事件
		sk.selector().wakeup(); // 使一个阻塞住的selector操作立即返回
	}

	/**
	 * 
	 * @Description: 转发 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
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
			client = new Socket(this.listenIp, 502); // 連接至目的地  
            System.out.println("Connected to "+this.listenIp);  
            client.setSoTimeout(3000);
            out = client.getOutputStream();
            
            in = client.getInputStream();
            
            out.write(arr); // 转发送来自客户端的请求  
            out.flush(); // 强制将缓冲区的数据输出
            
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
	 * @Description:纠正第5,第6个字节所描述的长度数据 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
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
