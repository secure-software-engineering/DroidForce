package de.tum.in.i22.uc.cm.distribution;

import java.security.InvalidParameterException;
import java.util.Objects;

import org.apache.commons.validator.routines.InetAddressValidator;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IName;


public class IPLocation extends Location {
	private final String _host;
	private final int _port;

	private static final InetAddressValidator validator = InetAddressValidator.getInstance();

	/**
	 * The localhost represented as {@link IPLocation}.
	 */
	public static final IPLocation localIpLocation = new IPLocation(Network.localInetAddress.getHostAddress());

	/**
	 * Creates an {@link IPLocation} from
	 * a string of format <host>#<port>, as
	 * returned by {@link IPLocation#asString()}.
	 *
	 * @see IPLocation#asString()
	 *
	 * @param s the string
	 */
	public IPLocation(String s) {
		super(ELocation.IP);

		String[] arr;
		boolean success = false;

		String hostValue = "";
		int portValue = 0;

		if (s != null && (arr = s.split("#")).length >= 1 && validator.isValid(arr[0])) {
			try {
				hostValue = arr[0];
				success = true;
			} catch (Exception e) {
				success = false;
			}

			if (arr.length >= 2) {
				portValue = Integer.valueOf(arr[1]);
			}
		}

		if (!success) {
			throw new InvalidParameterException("Unable to create IPLocation out of string [" + s + "].");
		}

		_host = hostValue;
		_port = portValue;
	}

	public IPLocation(String host, int port) {
		super(ELocation.IP);

		if (host == null || port < 0) {
			throw new InvalidParameterException("[" + host + "#" + port + "]");
		}
		_host = host;
		_port = port;
	}


	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_host", _host)
				.add("_port", _port)
				.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IPLocation) {
			IPLocation o = (IPLocation) obj;
			return Objects.equals(_host, o._host)
					&& Objects.equals(_port, o._port);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _host.hashCode() ^ _port;
	}

	public String getHost() {
		return _host;
	}

	public int getPort() {
		return _port;
	}



	/**
	 * As prescribed by {@link Location}.
	 * Returns <host>#<port>.
	 *
	 * @see IPLocation#IPLocation(String)
	 */
	@Override
	public String asString() {
		return _host + "#" + _port;
	}

	/**
	 * As prescribed by {@link IName}.
	 * Returns this {@link IPLocation}'s host
	 * prefixed by {@link Location#PREFIX_LOCATION}.
	 */
	@Override
	public String getName() {
		return PREFIX_LOCATION + _host;
	}
}
