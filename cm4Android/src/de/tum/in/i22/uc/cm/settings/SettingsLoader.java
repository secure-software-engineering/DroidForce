package de.tum.in.i22.uc.cm.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsLoader {
	private static final Logger _logger = LoggerFactory.getLogger(SettingsLoader.class);

	protected Map<String, Entry<?>> _settings;

	protected Properties _props;

	/**
	 * Tries to load <code>propertiesFileName</code> from the directory where the jar file is executed.
	 * If the file is not present there, it will be loaded from the jar file itself.
	 * This enables to easily override default properties which are specified in the file which
	 * is placed in the resource folder in the jar.
	 *
	 * @param propertiesFileName Name of the properties file to be loaded.
	 * @return Properties object with loaded properties.
	 * @throws IOException In case the file cannot be loaded.
	 */
	void initProperties(String propertiesFileName) throws IOException {
		_logger.debug("Loading properties file: " + propertiesFileName);

		InputStream is = null;
		File file = null;
		boolean fileFound = false;
		try {
			file = new File(propertiesFileName);
			fileFound = file.exists();
			_logger.debug("Searching properties file " + propertiesFileName + " ... " + (fileFound ? "Found" : "Not found") + ".");

			if (!fileFound) {
				file = new File(new File("."), propertiesFileName);
				fileFound = file.exists();
				_logger.debug("Searching properties file " + propertiesFileName	+ " in jar parent directory ... "  + (fileFound ? "Found" : "Not found") + ".");
			}

			if (!fileFound) {
				_logger.debug("Searching properties file " + propertiesFileName	+ " in resources ... ");
				is = SettingsLoader.class.getClassLoader().getResourceAsStream(propertiesFileName);
			}


			if (fileFound && is == null) {
				is = new FileInputStream(file);
			}

			if (is == null) {
				throw new IOException("Properties file not found.");
			}

			// load a properties file
			_props = new Properties();
			// load all the properties from this file
			_props.load(is);
			_logger.debug("Properties file '" + propertiesFileName + "' loaded.");

		} finally {
			if (is != null) {
				// we have loaded the properties, so close the file handler
				try {
					is.close();
				} catch (IOException e) {
					_logger.error("Failed to close input stream for properties file.", e);
				}
			}
		}
	}

	protected <T extends Object> T loadSettingFinalize(boolean success, String propName, T loadedValue, T defaultValue) {
		T returnValue=null;
		if (success) {
			_logger.info("Property [" + propName + "] loaded. Value: [" + loadedValue + "].");
			returnValue=loadedValue;
		}
		else {
			_logger.warn("Property [" + propName + "] not found. Using default value [" + defaultValue + "].");
			returnValue=defaultValue;
		}
		_settings.put(propName, putValue(returnValue));
		return returnValue;
	}

	public int loadSetting(String propName, int defaultValue) {
		int loadedValue = defaultValue;

		boolean success = false;

		try {
			if (_props != null) {
				loadedValue = Integer.valueOf((String) _props.get(propName));
				success = true;
			}
		}
		catch (Exception e) {
			success = false;
		}

		return loadSettingFinalize(success, propName, loadedValue, defaultValue);
	}

	public String loadSetting(String propName, String defaultValue) {
		String loadedValue = defaultValue;

		boolean success = false;

		try {
			if (_props != null) {
				loadedValue = (String) _props.get(propName);
				if (loadedValue != null) {
					success = true;
				}
			}
		}
		catch (Exception e) { }

		return loadSettingFinalize(success, propName, loadedValue, defaultValue);
	}

	public boolean loadSetting(String propName, boolean defaultValue) {
		boolean loadedValue = defaultValue;

		boolean success = false;

		try {
			if (_props != null) {				
				String s = _props.getProperty(propName);
				if (s != null) {
					loadedValue = Boolean.valueOf(s);
					success = true;
				}
			}
		}
		catch (Exception e) {	}

		return loadSettingFinalize(success, propName, loadedValue, defaultValue);
	}

	public <T> void setProperty(String propName, T value) {
		_settings.put(propName, new Entry<T>(value.getClass(), value));
	}

	protected Entry<Object> putValue(Object obj) {
		return new Entry<Object>(obj.getClass(), obj);
	}

	protected <T> T getValue(String propName) {
		@SuppressWarnings("unchecked")
		Entry<T> e = (Entry<T>)  _settings.get(propName);
		if (e==null) return null;
		return e.getValue();
	}

	
	protected class Entry<T> {
		private Class<T> _type;
		private T _value;

		@SuppressWarnings("unchecked")
		public Entry(Class<? extends Object> type, T value) {
			_type = (Class<T>) type;
			_value = value;
		}

		public T getValue() {
			return _value;
		}

		public Class<T> getType() {
			return _type;
		}

	}



}
