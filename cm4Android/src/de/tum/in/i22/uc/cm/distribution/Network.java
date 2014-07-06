package de.tum.in.i22.uc.cm.distribution;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.validator.routines.InetAddressValidator;

import android.os.AsyncTask;

import com.google.common.collect.Sets;

/**
 *
 * @author Florian Kelbert
 *
 */
public class Network {

	public static final InetAddress localInetAddress = getLocalInetAddress();

	public static final Set<String> LOCAL_IP_ADDRESSES = Sets.newHashSet(
				"127.0.0.1",
				"localhost",
				"0000:0000:0000:0000:0000:0000:0000:0001",
				"0:0:0:0:0:0:0:1",
				"::1");


	private static InetAddress getLocalInetAddress() {
		// Do not perform network operations on the main thread in Android
		AsyncTask<Void, Void, InetAddress> async = new AsyncTask<Void, Void, InetAddress>() {

			@Override
			protected InetAddress doInBackground(Void... params) {
				InetAddress result = null;
				
				try {
					result = InetAddress.getLocalHost();
				} catch (UnknownHostException e) { }

				if (result == null) {
					Enumeration<NetworkInterface> nics;
					try {
						nics = NetworkInterface.getNetworkInterfaces();
					} catch (SocketException e) {
						return null;
					}

					while(nics.hasMoreElements() && result == null) {
					    NetworkInterface nic = nics.nextElement();
					    Enumeration<InetAddress> addresses = nic.getInetAddresses();

					    while (addresses.hasMoreElements() && result == null) {
					        InetAddress addr = addresses.nextElement();

					        if (!addr.isSiteLocalAddress() && !addr.isAnyLocalAddress() && !addr.isLoopbackAddress()
					        		&& InetAddressValidator.getInstance().isValidInet4Address(addr.getHostAddress())) {
					        	result = addr;
					        }
					    }
					}
				}

				return result;
			}
			
		};
		try {
			return async.execute().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}