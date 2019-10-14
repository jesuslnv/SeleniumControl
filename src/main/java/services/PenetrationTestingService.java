package services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.clientapi.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class PenetrationTestingService {
    private static final Logger LOGGER = LogManager.getLogger();
    /* The IP to establish the ZAP connection */
    private static String HTTP_IP = "127.0.0.1";
    /* The PORT to establish the ZAP connection */
    private static int HTTP_PORT = 9090;
    /* The STRENGTH level used in the scanner */
    private static String SCANNER_STRENGTH = "High";
    /* The THRESHOLD level used in the scanner */
    private static String SCANNER_THRESHOLD = "Low";
    /* The RISK LEVEL minimum to be considered in the Alert List */
    private static String RISK_LEVEL = "MEDIUM";
    //-------------------------------------------------------------------------------
    private static HashMap<String, String> ATTACK_CODES = new HashMap<>();
    private static ClientApi clientApi = new ClientApi(HTTP_IP, HTTP_PORT);
    private static String previousUrlScanned = "";
    private static HashMap<String, List<Alert>> hashMapScannedAlertsFound;

    //<editor-fold desc="GETTER AND SETTERS">
    public static String getHttpIp() {
        return HTTP_IP;
    }

    public static void setHttpIp(String httpIp) {
        HTTP_IP = httpIp;
    }

    public static int getHttpPort() {
        return HTTP_PORT;
    }

    public static void setHttpPort(int httpPort) {
        HTTP_PORT = httpPort;
    }

    public static String getScannerStrength() {
        return SCANNER_STRENGTH;
    }

    public static void setScannerStrength(String scannerStrength) {
        SCANNER_STRENGTH = scannerStrength;
    }

    public static String getScannerThreshold() {
        return SCANNER_THRESHOLD;
    }

    public static void setScannerThreshold(String scannerThreshold) {
        SCANNER_THRESHOLD = scannerThreshold;
    }

    public static String getRiskLevel() {
        return RISK_LEVEL;
    }

    public static void setRiskLevel(String riskLevel) {
        RISK_LEVEL = riskLevel;
    }
    //</editor-fold>

    /**
     * @param urlToScan Is the url to be scanned
     * @return Returns a Map with an Alert List for each scan type (1. Passive Scan, 2. Active Scan, 3. Spider Scan)
     */
    public static HashMap<String, List<Alert>> runScanner(String urlToScan) {
        hashMapScannedAlertsFound = new HashMap<>();
        //-----------------------------------------------------------------------------------------------------------
        //Verify if the "urlToScan" is equals to the "previousUrlScanned" to avoid multiple scans to the same URL.
        if (urlToScan.equals(previousUrlScanned)) {
            return null;
        }
        //-----------------------------------------------------------------------------------------------------------
        //Run Passive Scan First (Because is the most basic and simple Scan)
        runPassiveScan();
        //-----------------------------------------------------------------------------------------------------------
        //Call the function to configure the MAP with the ATTACK_CODES for "Active Scan"
        configureMapAttackCodes();
        //Run Active Scan with each specified Penetration Test
        ATTACK_CODES.forEach((attackType, attackTypeId) -> {
            runActiveScan(urlToScan, attackType, attackTypeId);
        });
        //-----------------------------------------------------------------------------------------------------------
        //Run Spider Scan at Last
        runSpiderScan(urlToScan);
        //-----------------------------------------------------------------------------------------------------------
        //Set the current URL scanned to the previous
        previousUrlScanned = urlToScan;
        return hashMapScannedAlertsFound;
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
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("PASSIVE SCAN");
        } catch (ClientApiException ex) {
            LOGGER.error("Passive Scan \"ClientApiException\" Error: {}", ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        } catch (InterruptedException ex) {
            LOGGER.error("Passive Scan \"InterruptedException\" Error: {}", ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
            Thread.currentThread().interrupt();
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
                clientApi.ascan.setScannerAttackStrength(id, SCANNER_STRENGTH, null);
                clientApi.ascan.setScannerAlertThreshold(id, SCANNER_THRESHOLD, null);
            }
            ApiResponse apiResponse = clientApi.ascan.scan(urlToScan, "True", "False", null, null, null);
            String scanId = ((ApiResponseElement) apiResponse).getValue();
            int progress = 0;
            int scanTime = 0;
            while (progress < 100) {
                Thread.sleep(1000);
                scanTime++;
                progress = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanId)).getValue());
                //LOGGER.info("Active Scan progress: " + progress + "%");
            }
            LOGGER.info("Active Scan Completed in {} seconds", scanTime);
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("ACTIVE SCAN | " + scanTypeName);
        } catch (ClientApiException ex) {
            LOGGER.error("Active Scan \"ClientApiException\" Error: {}", ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        } catch (InterruptedException ex) {
            LOGGER.error("Active Scan \"InterruptedException\" Error: {}", ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
            Thread.currentThread().interrupt();
        }
    }

    private static void runSpiderScan(String urlToScan) {
        LOGGER.info("--------------------------Starting Spider Scan---------------------------");
        LOGGER.info("Scanning URL: {}", urlToScan);
        try {
            //Remove all Historical Alerts generated Before
            clientApi.alert.deleteAllAlerts();
            //Enable specific Active Scanner
            ApiResponse apiResponse = clientApi.spider.scan(urlToScan, null, null, null, null);
            String scanId = ((ApiResponseElement) apiResponse).getValue();
            int progress = 0;
            int scanTime = 0;
            //Polling the status until it completes
            while (progress < 100) {
                Thread.sleep(1000);
                scanTime++;
                progress = Integer.parseInt(((ApiResponseElement) clientApi.spider.status(scanId)).getValue());
                //LOGGER.info("Spider progress : " + progress + "%");
            }
            LOGGER.info("Spider Scan Completed in {} seconds", scanTime);
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("SPIDER SCAN");
        } catch (ClientApiException ex) {
            LOGGER.error("Spider Scan \"ClientApiException\" Error: {}", ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        } catch (InterruptedException ex) {
            LOGGER.error("Spider Scan \"InterruptedException\" Error: {}", ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
            Thread.currentThread().interrupt();
        }
    }

    private static void updateMapSecurityAlertList(String mapScanType) {
        //Get alert List to shown in the LOG
        List<Alert> lstAlerts = new ArrayList<>();
        //By default get "HIGH" risks only
        Alert.Risk risk = Alert.Risk.High;
        if ("MEDIUM".equalsIgnoreCase(RISK_LEVEL)) {
            risk = Alert.Risk.Medium;
        } else if ("LOW".equalsIgnoreCase(RISK_LEVEL)) {
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
        hashMapScannedAlertsFound.put(mapScanType, lstAlerts);
    }

    private static void configureMapAttackCodes() {
        //Configure Attack Codes in Map
        ATTACK_CODES = new HashMap<>();
        ATTACK_CODES.put("DIRECTORY_BROWSING", "0");
        ATTACK_CODES.put("PATH_TRAVERSAL", "6");
        ATTACK_CODES.put("REMOTE_FILE_INCLUSION", "7");
        ATTACK_CODES.put("SOURCE_CODE_DISCLOSURE", "10045");
        ATTACK_CODES.put("REMOTE_CODE_EXECUTION", "20018");
        ATTACK_CODES.put("EXTERNAL_REDIRECT", "20019");
        ATTACK_CODES.put("BUFFER_OVERFLOW", "30001");
        ATTACK_CODES.put("FORMAT_STRING_ERROR", "30002");
        ATTACK_CODES.put("CRLF_INJECTION", "40003");
        ATTACK_CODES.put("PARAMETER_TAMPERING", "40008");
        ATTACK_CODES.put("SERVER_SIDE_INCLUDE", "40009");
        ATTACK_CODES.put("CROSS_SITE_SCRIPTING", "40012,40014,40016,40017");
        ATTACK_CODES.put("SQL_INJECTION", "40018");
        ATTACK_CODES.put("SCRIPT_ACTIVE_SCAN_RULES", "50000");
        ATTACK_CODES.put("SERVER_SIDE_CODE_INJECTION", "90019");
        ATTACK_CODES.put("REMOTE_OS_COMMAND_INJECTION", "90020");
        //ATTACK_CODES.put("REMOTE_CODE_EXECUTION","20018");
        //ATTACK_CODES.put("LDAP_INJECTION","40015");
        //ATTACK_CODES.put("INSECURE_HTTP_METHODS","90028");
        //ATTACK_CODES.put("XPATH_INJECTION","90021");
        //ATTACK_CODES.put("PADDING_ORACLE","90024");
        //ATTACK_CODES.put("SHELL_SHOCK","10048");
        //ATTACK_CODES.put("XML_EXTERNAL_ENTITY","90023");
        //ATTACK_CODES.put("PARAMETER_POLLUTION","20014");
        //ATTACK_CODES.put("EL_INJECTION","90025");
        //ATTACK_CODES.put("PADDING_ORACLE","90024");
    }
}