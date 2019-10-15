package services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.clientapi.core.*;

import java.util.*;

public final class PenetrationTestingService {
    private static final Logger LOGGER = LogManager.getLogger();
    /* The IP to establish the ZAP connection */
    private static String httpIp = "127.0.0.1";
    /* The PORT to establish the ZAP connection */
    private static int httpPort = 9090;
    /* The STRENGTH level used in the scanner */
    private static String scannerStrength = "High";
    /* The THRESHOLD level used in the scanner */
    private static String scannerThreshold = "Low";
    /* The RISK LEVEL minimum to be considered in the Alert List */
    private static String riskLevel = "MEDIUM";
    /* Enables spcific Scanners (Passive Scan, Active Scan) */
    private static boolean enablePassiveScan = true;
    private static boolean enableActiveScan = true;
    //-------------------------------------------------------------------------------
    private static Map<String, String> attackCodes = new HashMap<>();
    private static ClientApi clientApi;
    private static String previousUrlScanned = "";
    private static Map<String, List<Alert>> mapScannedAlertsFound;

    private PenetrationTestingService() {
    }

    //<editor-fold desc="GETTER AND SETTERS">
    public static String getHttpIp() {
        return httpIp;
    }

    public static void setHttpIp(String httpIp) {
        PenetrationTestingService.httpIp = httpIp;
    }

    public static int getHttpPort() {
        return httpPort;
    }

    public static void setHttpPort(int httpPort) {
        PenetrationTestingService.httpPort = httpPort;
    }

    public static String getScannerStrength() {
        return scannerStrength;
    }

    public static void setScannerStrength(String scannerStrength) {
        PenetrationTestingService.scannerStrength = scannerStrength;
    }

    public static String getScannerThreshold() {
        return scannerThreshold;
    }

    public static void setScannerThreshold(String scannerThreshold) {
        PenetrationTestingService.scannerThreshold = scannerThreshold;
    }

    public static String getRiskLevel() {
        return riskLevel;
    }

    public static void setRiskLevel(String riskLevel) {
        PenetrationTestingService.riskLevel = riskLevel;
    }

    public static boolean isEnablePassiveScan() {
        return enablePassiveScan;
    }

    public static void setEnablePassiveScan(boolean enablePassiveScan) {
        PenetrationTestingService.enablePassiveScan = enablePassiveScan;
    }

    public static boolean isEnableActiveScan() {
        return enableActiveScan;
    }

    public static void setEnableActiveScan(boolean enableActiveScan) {
        PenetrationTestingService.enableActiveScan = enableActiveScan;
    }
    //</editor-fold>

    /**
     * @param urlToScan Is the url to be scanned
     * @return Returns a Map with an Alert List for each scan type (1. Passive Scan, 2. Active Scan)
     */
    public static Map<String, List<Alert>> runScanner(String urlToScan) {
        //Instances the "clientApi" with the previously configured IP and PORT
        clientApi = new ClientApi(httpIp, httpPort);
        mapScannedAlertsFound = new HashMap<>();
        //-----------------------------------------------------------------------------------------------------------
        //Verify if the "urlToScan" is equals to the "previousUrlScanned" to avoid multiple scans to the same URL.
        if (urlToScan.equals(previousUrlScanned)) {
            return null;
        }
        //-----------------------------------------------------------------------------------------------------------
        //Run Passive Scan First (Because is the most basic and simple Scan)
        if (enablePassiveScan) {
            runPassiveScan();
        }
        //-----------------------------------------------------------------------------------------------------------
        //Call the function to configure the MAP with the ATTACK_CODES for "Active Scan"
        if (enableActiveScan) {
            configureMapAttackCodes();
            //Run Active Scan with each specified Penetration Test
            attackCodes.forEach((attackType, attackTypeId) ->
                    runActiveScan(urlToScan, attackType, attackTypeId)
            );
        }
        //-----------------------------------------------------------------------------------------------------------
        //Set the current URL scanned to the previous
        previousUrlScanned = urlToScan;
        return mapScannedAlertsFound;
    }

    private static void runPassiveScan() {
        LOGGER.info("--------------------------Starting Passive Scan--------------------------");
        try {
            //Remove all Historical Alerts generated Before
            clientApi.alert.deleteAllAlerts();
            //Enable all Scanners in Passive Mode
            clientApi.pscan.enableAllScanners();
            ApiResponse apiResponse = clientApi.pscan.recordsToScan();
            int scanTime = 0;
            while (!apiResponse.toString().equals("0")) {
                Thread.sleep(1000);
                scanTime++;
                apiResponse = clientApi.pscan.recordsToScan();
            }
            LOGGER.info("Passive Scan Completed in {} seconds", scanTime);
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("PASSIVE SCAN");
        } catch (ClientApiException psEx) {
            LOGGER.error("Passive Scan \"ClientApiException\" Error: {}", psEx.getMessage());
        } catch (InterruptedException psEx) {
            LOGGER.error("Passive Scan \"InterruptedException\" Error: {}", psEx.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("-------------------------------------------------------------------------");
        }
    }

    private static void runActiveScan(String urlToScan, String scanTypeName, String scanTypeId) {
        LOGGER.info("--------------------------Starting Active Scan---------------------------");
        LOGGER.info("Scanning URL: {}", urlToScan);
        LOGGER.info("Scan Type: {}", scanTypeName);
        LOGGER.info("Scan Id: {}", scanTypeId);
        try {
            //Remove all Historical Alerts generated Before
            clientApi.alert.deleteAllAlerts();
            //Disable all other Scanners by Default
            clientApi.ascan.disableAllScanners(null);
            //Set Attack Mode in OwaspZap
            clientApi.core.setMode("attack");
            //Enable specific Active Scanner
            clientApi.ascan.enableScanners(scanTypeId, null);
            for (String id : scanTypeId.split(",")) {
                clientApi.ascan.setScannerAttackStrength(id, scannerStrength, null);
                clientApi.ascan.setScannerAlertThreshold(id, scannerThreshold, null);
            }
            ApiResponse apiResponse = clientApi.ascan.scan(urlToScan, "True", "False", null, null, null);
            String scanId = ((ApiResponseElement) apiResponse).getValue();
            int progress = 0;
            int scanTime = 0;
            while (progress < 100) {
                Thread.sleep(1000);
                scanTime++;
                progress = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanId)).getValue());
            }
            LOGGER.info("Active Scan Completed in {} seconds", scanTime);
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("ACTIVE SCAN | " + scanTypeName);
        } catch (ClientApiException asEx) {
            LOGGER.error("Active Scan \"ClientApiException\" Error: {}", asEx.getMessage());
        } catch (InterruptedException asEx) {
            LOGGER.error("Active Scan \"InterruptedException\" Error: {}", asEx.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("-------------------------------------------------------------------------");
        }
    }

    private static void updateMapSecurityAlertList(String mapScanType) {
        //Get alert List to shown in the LOG
        List<Alert> lstAlerts = new ArrayList<>();
        //By default get "HIGH" risks only
        Alert.Risk risk = Alert.Risk.High;
        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            risk = Alert.Risk.Medium;
        } else if ("LOW".equalsIgnoreCase(riskLevel)) {
            risk = Alert.Risk.Low;
        }
        try {
            List<Alert> tmpLstAlerts = clientApi.getAlerts("", -1, -1);
            for (Alert alert : tmpLstAlerts) {
                if (alert.getRisk().ordinal() >= risk.ordinal()) {
                    lstAlerts.add(alert);
                }
            }
        } catch (ClientApiException ex) {
            LOGGER.error("Error updating Map Security Alert List: {}", ex.getMessage());
        }
        //Add Alert List Found to the Map
        mapScannedAlertsFound.put(mapScanType, lstAlerts);
    }

    private static void configureMapAttackCodes() {
        //Configure Attack Codes in Map
        attackCodes = new HashMap<>();
        attackCodes.put("DIRECTORY_BROWSING", "0");
        attackCodes.put("PATH_TRAVERSAL", "6");
        attackCodes.put("REMOTE_FILE_INCLUSION", "7");
        attackCodes.put("SOURCE_CODE_DISCLOSURE", "10045");
        attackCodes.put("REMOTE_CODE_EXECUTION", "20018");
        attackCodes.put("EXTERNAL_REDIRECT", "20019");
        attackCodes.put("BUFFER_OVERFLOW", "30001");
        attackCodes.put("FORMAT_STRING_ERROR", "30002");
        attackCodes.put("CRLF_INJECTION", "40003");
        attackCodes.put("PARAMETER_TAMPERING", "40008");
        attackCodes.put("SERVER_SIDE_INCLUDE", "40009");
        attackCodes.put("CROSS_SITE_SCRIPTING", "40012,40014,40016,40017");
        attackCodes.put("SQL_INJECTION", "40018");
        attackCodes.put("SCRIPT_ACTIVE_SCAN_RULES", "50000");
        attackCodes.put("SERVER_SIDE_CODE_INJECTION", "90019");
        attackCodes.put("REMOTE_OS_COMMAND_INJECTION", "90020");
    }
}