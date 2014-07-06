package de.tum.in.i22.uc.cm.settings;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.datatypes.basic.DataBasic;
import de.tum.in.i22.uc.cm.datatypes.basic.NameBasic;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IData;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IName;
import de.tum.in.i22.uc.cm.distribution.ECommunicationProtocol;
import de.tum.in.i22.uc.cm.distribution.EDistributionStrategy;
import de.tum.in.i22.uc.cm.distribution.IPLocation;
import de.tum.in.i22.uc.cm.distribution.LocalLocation;
import de.tum.in.i22.uc.cm.distribution.Location;
import de.tum.in.i22.uc.cm.distribution.Location.ELocation;
import de.tum.in.i22.uc.cm.pip.EInformationFlowModel;

/**
 * 
 * @author Florian Kelbert Settings are read from the specified properties file.
 *         If no file is specified, file "uc.properties" is used.
 * 
 */
public class Settings extends SettingsLoader {

	private static Logger _logger = LoggerFactory.getLogger(Settings.class);

	private static Settings _instance = null;

	private static String _propertiesFile = "uc.properties";

	private static final String PROP_NAME_pdpListenerPort = "pdpListenerPort";
	private static final String PROP_NAME_pmpListenerPort = "pmpListenerPort";
	private static final String PROP_NAME_pipListenerPort = "pipListenerPort";
	private static final String PROP_NAME_anyListenerPort = "anyListenerPort";

	private static final String PROP_NAME_pxpListenerPort = "pxpListenerPort";
	private static final String PROP_NAME_anyListenerEnabled = "anyListenerEnabled";

	private static final String PROP_NAME_pdpLocation = "pdpLocation";
	private static final String PROP_NAME_pipLocation = "pipLocation";
	private static final String PROP_NAME_pmpLocation = "pmpLocation";

	private static final String PROP_NAME_pipEnabledInformationFlowModels = "pipEnabledInformationFlowModels";
	private static final String PROP_NAME_pipEventHandlerSuffix = "pipEventHandlerSuffix";
	private static final String PROP_NAME_pipEventHandlerPackage = "pipEventHandlerPackage";
	private static final String PROP_NAME_pipInitializerEvent = "pipInitializerEvent";
	private static final String PROP_NAME_pipPersistenceDirectory = "pipPersistenceDirectory";

	private static final String PROP_NAME_pipPrintAfterUpdate = "pipPrintAfterUpdate";

	private static final String PROP_NAME_separator1 = "separator1";
	private static final String PROP_NAME_separator2 = "separator2";
	private static final String PROP_NAME_pipInitialRepresentationSeparator1 = "pipInitialRepresentationSeparator1";
	private static final String PROP_NAME_pipInitialRepresentationSeparator2 = "pipInitialRepresentationSeparator2";

	private static final String PROP_NAME_prefixSeparator = "prefixSeparator";

	private static final String PROP_NAME_pepParameterKey = "pep";
	private static final String PROP_NAME_allowImpliesActualParameterKey = "allowImpliesActual";

	private static final String PROP_NAME_pipInitialRepresentations = "pipInitialRepresentations";

	private static final String PROP_NAME_communicationProtocol = "communicationProtocol";

	private static final String PROP_NAME_distributionStrategy = "distributionStrategy";
	private static final String PROP_NAME_pipDistributionMaxConnections = "pipDistributionMaxConnections";

	private static final String PROP_NAME_pdpDistributionMaxConnections = "pdpDistributionMaxConnections";

	private static final String PROP_NAME_pmpDistributionMaxConnections = "pmpDistributionMaxConnections";

	private static final String PROP_NAME_connectionAttemptInterval = "connectionAttemptInterval";

	private static final String PROP_NAME_starEvent = "starEvent";

	private static final String PROP_NAME_scopeDelimiterName = "scopeDelimiterName";
	private static final String PROP_NAME_scopeOpenDelimiter = "scopeOpenDelimiter";
	private static final String PROP_NAME_scopeCloseDelimiter = "scopeCloseDelimiter";
	private static final String PROP_NAME_scopeDirectionName = "scopeDirectionName";
	private static final String PROP_NAME_scopeGenericInDirection = "scopeGenericInDirection";
	private static final String PROP_NAME_scopeGenericOutDirection = "scopeGenericOutDirection";

	private static final String PROP_NAME_showFullIFModel = "showFullIFModel";

	private static final String PROP_NAME_policySpecificationStarDataClass = "policySpecificationStarDataClass";

	private Settings() {
		_settings = new HashMap<>();

		try {
			initProperties(_propertiesFile);
		} catch (IOException e) {
			_logger.warn("Unable to load properties file [" + _propertiesFile
					+ "]. Using defaults.");
		}

		loadProperties();
	}

	public static void setPropertiesFile(String propertiesFile) {
		boolean success = false;
		if (_instance == null) {
			synchronized (Settings.class) {
				if (_instance == null) {
					_propertiesFile = propertiesFile;
					success = true;
				}
			}
		}

		if (success) {
			_logger.warn("Must set properties file before getting the first Settings instance.");
		}
	}

	public static Settings getInstance() {
		/*
		 * This implementation may seem odd, overengineered, redundant, or all
		 * of it. Yet, it is the best way to implement a thread-safe singleton,
		 * cf.
		 * http://www.journaldev.com/171/thread-safety-in-java-singleton-classes
		 * -with-example-code -FK-
		 */
		if (_instance == null) {
			synchronized (Settings.class) {
				if (_instance == null)
					_instance = new Settings();
			}
		}
		return _instance;
	}

	private void loadProperties() {
		loadSetting(PROP_NAME_pmpListenerPort, 21001);
		loadSetting(PROP_NAME_pipListenerPort, 21002);
		loadSetting(PROP_NAME_pdpListenerPort, 21003);
		loadSetting(PROP_NAME_anyListenerPort, 21004);

		loadSetting(PROP_NAME_pxpListenerPort, 30003);
		loadSetting(PROP_NAME_anyListenerEnabled, true);

		loadSetting(PROP_NAME_pdpLocation, LocalLocation.getInstance());
		loadSetting(PROP_NAME_pipLocation, LocalLocation.getInstance());
		loadSetting(PROP_NAME_pmpLocation, LocalLocation.getInstance());

		loadSetting(PROP_NAME_pipEnabledInformationFlowModels,
				"scope@structure");
		loadSetting(PROP_NAME_pipEventHandlerSuffix, "EventHandler");
		loadSetting(PROP_NAME_pipEventHandlerPackage,
				"de.tum.in.i22.uc.pip.eventdef.");
		loadSetting(PROP_NAME_pipInitializerEvent, "SchemaInitializer");
		loadSetting(PROP_NAME_pipPersistenceDirectory, "pipdb");

		loadSetting(PROP_NAME_pipPrintAfterUpdate, true);

		loadSetting(PROP_NAME_separator1, "@");
		loadSetting(PROP_NAME_separator2, "#");
		loadSetting(PROP_NAME_pipInitialRepresentationSeparator1, ";");
		loadSetting(PROP_NAME_pipInitialRepresentationSeparator2, ":");

		loadSetting(PROP_NAME_prefixSeparator, "_");

		loadSetting(PROP_NAME_pepParameterKey, "PEP");
		loadSetting(PROP_NAME_allowImpliesActualParameterKey, "false");

		loadSetting(PROP_NAME_pipInitialRepresentations,
				new HashMap<IName, IData>() {
					private static final long serialVersionUID = -2810488356921449504L;
					{
						put(new NameBasic("TEST_C"), new DataBasic("TEST_D"));
					}
				});

		loadSetting(PROP_NAME_communicationProtocol,
				ECommunicationProtocol.THRIFT, ECommunicationProtocol.class);

		loadSetting(PROP_NAME_distributionStrategy, EDistributionStrategy.PUSH,
				EDistributionStrategy.class);
		loadSetting(PROP_NAME_pipDistributionMaxConnections, 5);

		loadSetting(PROP_NAME_pdpDistributionMaxConnections, 5);

		loadSetting(PROP_NAME_pmpDistributionMaxConnections, 5);

		loadSetting(PROP_NAME_connectionAttemptInterval, 1000);

		loadSetting(PROP_NAME_starEvent, "*");

		loadSetting(PROP_NAME_scopeDelimiterName, "delimiter");
		loadSetting(PROP_NAME_scopeOpenDelimiter, "start");
		loadSetting(PROP_NAME_scopeCloseDelimiter, "end");
		loadSetting(PROP_NAME_scopeDirectionName, "direction");
		loadSetting(PROP_NAME_scopeGenericInDirection, "IN");
		loadSetting(PROP_NAME_scopeGenericOutDirection, "OUT");

		loadSetting(PROP_NAME_showFullIFModel, false);

		loadSetting(PROP_NAME_policySpecificationStarDataClass, "*");

	}

	public Location loadSetting(String propName, Location defaultValue) {
		Location loadedValue = defaultValue;

		boolean success = false;

		try {
			if (_props != null) {
				loadedValue = new IPLocation(_props.getProperty(propName));
				if (loadedValue != null) {
					success = true;
				}
			}
		} catch (Exception e) {
		}

		return loadSettingFinalize(success, propName, loadedValue, defaultValue);
	}

	public <E extends Enum<E>> E loadSetting(String propName, E defaultValue,
			Class<E> cls) {
		E loadedValue = defaultValue;

		boolean success = false;

		try {
			if (_props != null) {
				loadedValue = E.valueOf(cls, (String) _props.get(propName));
				if (loadedValue != null) {
					success = true;
				}
			}
		} catch (Exception e) {
			success = false;
		}

		return loadSettingFinalize(success, propName, loadedValue, defaultValue);
	}

	/**
	 * Loads the initial representations for the Pip. They are expected to be in
	 * the format <ContainerName1>:<DataId1>;<ContainerName2>:<DataId2>; ...
	 * 
	 * @param propName
	 *            the property name
	 * @param defaultValue
	 * @return
	 */
	public Map<IName, IData> loadSetting(String propName,
			Map<IName, IData> defaultValue) {
		Map<IName, IData> loadedValue = new HashMap<>();

		boolean success = false;
		String stringRead = null;

		try {
			if (_props != null)
				stringRead = (String) _props.get(propName);
		} catch (Exception e) {
			stringRead = null;
			success = false;
		}

		if (stringRead != null && stringRead.length() > 0) {

			// entries are divided by semicolon (;)
			String[] entries = stringRead
					.split(getPipInitialRepresentationSeparator1());

			if (entries != null && entries.length > 0) {

				for (String entry : entries) {

					// each entry is divided by exactly one colon
					// first part: container name; second part: data ID
					String[] entryParts = entry
							.split(getPipInitialRepresentationSeparator2());
					if (entryParts != null && entryParts.length == 2) {
						loadedValue.put(new NameBasic(entryParts[0]),
								new DataBasic(entryParts[1]));
					} else {
						_logger.debug("Incorrect entry format: " + entry);
					}
				}
			}
		}

		success = loadedValue.size() > 0;

		return loadSettingFinalize(success, propName, loadedValue, defaultValue);
	}

	public String getAllowImpliesActualParameterKey() {
		return PROP_NAME_allowImpliesActualParameterKey;
	}

	public String getPropertiesFileName() {
		return _propertiesFile;
	}

	public int getPmpListenerPort() {
		return getValue(PROP_NAME_pmpListenerPort);
	}

	public int getPipListenerPort() {
		return getValue(PROP_NAME_pipListenerPort);
	}

	public int getPdpListenerPort() {
		return getValue(PROP_NAME_pdpListenerPort);
	}

	public int getPxpListenerPort() {
		return getValue(PROP_NAME_pxpListenerPort);
	}

	public int getAnyListenerPort() {
		return getValue(PROP_NAME_anyListenerPort);
	}

	public boolean isPmpListenerEnabled() {
		Location l = getValue(PROP_NAME_pmpLocation);
		return ((l == null) ? false : (l.getLocation() == ELocation.LOCAL));
	}

	public boolean isPipListenerEnabled() {
		Location l = getValue(PROP_NAME_pipLocation);
		return ((l == null) ? false : (l.getLocation() == ELocation.LOCAL));
	}

	public boolean isPdpListenerEnabled() {
		Location l = getValue(PROP_NAME_pdpLocation);
		return ((l == null) ? false : (l.getLocation() == ELocation.LOCAL));
	}

	public boolean isAnyListenerEnabled() {
		return getValue(PROP_NAME_anyListenerEnabled);
	}

	public EDistributionStrategy getDistributionStrategy() {
		return getValue(PROP_NAME_distributionStrategy);
	}

	public String getPipEventHandlerPackage() {
		return getValue(PROP_NAME_pipEventHandlerPackage);
	}

	public String getPipEventHandlerSuffix() {
		return getValue(PROP_NAME_pipEventHandlerSuffix);
	}

	public Set<EInformationFlowModel> getEnabledInformationFlowModels() {
		return EInformationFlowModel
				.from((String) getValue(PROP_NAME_pipEnabledInformationFlowModels));
	}

	public Location getPdpLocation() {
		return getValue(PROP_NAME_pdpLocation);
	}

	public Location getPipLocation() {
		return getValue(PROP_NAME_pipLocation);
	}

	public Location getPmpLocation() {
		return getValue(PROP_NAME_pmpLocation);
	}

	public String getPipInitializerEvent() {
		return getValue(PROP_NAME_pipInitializerEvent);
	}

	public String getPipPersistenceDirectory() {
		return getValue(PROP_NAME_pipPersistenceDirectory);
	}

	public boolean getPipPrintAfterUpdate() {
		return getValue(PROP_NAME_pipPrintAfterUpdate);
	}

	public ECommunicationProtocol getCommunicationProtocol() {
		return getValue(PROP_NAME_communicationProtocol);
	}

	@SuppressWarnings("unchecked")
	public Map<IName, IData> getPipInitialRepresentations() {
		return Collections
				.unmodifiableMap((Map<IName, IData>) getValue(PROP_NAME_pipInitialRepresentations));
	}

	public int getPipDistributionMaxConnections() {
		return getValue(PROP_NAME_pipDistributionMaxConnections);
	}

	public int getPdpDistributionMaxConnections() {
		return getValue(PROP_NAME_pdpDistributionMaxConnections);
	}

	public int getPmpDistributionMaxConnections() {
		return getValue(PROP_NAME_pmpDistributionMaxConnections);
	}

	public int getConnectionAttemptInterval() {
		return getValue(PROP_NAME_connectionAttemptInterval);
	}

	public String getSeparator1() {
		return getValue(PROP_NAME_separator1);
	}

	public String getSeparator2() {
		return getValue(PROP_NAME_separator2);
	}

	public String getPrefixSeparator() {
		return getValue(PROP_NAME_prefixSeparator);
	}

	public String getPep() {
		return getValue(PROP_NAME_pepParameterKey);
	}

	public String getAllowImpliesActual() {
		return getValue(PROP_NAME_allowImpliesActualParameterKey);
	}

	public String getPipInitialRepresentationSeparator1() {
		return getValue(PROP_NAME_pipInitialRepresentationSeparator1);
	}

	public String getPipInitialRepresentationSeparator2() {
		return getValue(PROP_NAME_pipInitialRepresentationSeparator2);
	}

	public String getStarEvent() {
		return getValue(PROP_NAME_starEvent);
	}

	public String getScopeDelimiterName() {
		return getValue(PROP_NAME_scopeDelimiterName);
	}

	public String getScopeOpenDelimiter() {
		return getValue(PROP_NAME_scopeOpenDelimiter);
	}

	public String getScopeCloseDelimiter() {
		return getValue(PROP_NAME_scopeCloseDelimiter);
	}

	public String getScopeDirectionName() {
		return getValue(PROP_NAME_scopeDirectionName);
	}

	public String getScopeGenericInDirection() {
		return getValue(PROP_NAME_scopeGenericInDirection);
	}

	public String getScopeGenericOutDirection() {
		return getValue(PROP_NAME_scopeGenericOutDirection);
	}

	public boolean getShowFullIFModel() {
		return getValue(PROP_NAME_showFullIFModel);
	}

	public String getPolicySpecificationStarDataClass() {
		return getValue(PROP_NAME_policySpecificationStarDataClass);
	}

}
