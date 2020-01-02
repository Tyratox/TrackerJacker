package track.er.ja.cker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class DiscoveryThread implements Runnable{
	
	DatagramSocket socket;

	@Override
	public void run() {
		try {
			socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0"));
			
			while (true) {
		        //Receive a packet
				byte[] recvBuf = new byte[15000];
		        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
		        socket.receive(packet);
		        
		        //See if the packet holds the right command (message)
		        String message = new String(packet.getData()).trim();
		        if (message.equals("HACKED?")) {
		        	byte[] sendData = "TRACKERJACKER".getBytes();

		            //Send a response
		            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
		            socket.send(sendPacket);
		        }
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static DiscoveryThread getInstance() {
		return DiscoveryThreadHolder.INSTANCE;
	}
	
	 private static class DiscoveryThreadHolder {
		 private static final DiscoveryThread INSTANCE = new DiscoveryThread();
	 }


}
