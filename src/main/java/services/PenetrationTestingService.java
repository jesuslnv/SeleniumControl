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
    private static String http_ip = "127.0.0.1";
    /* The PORT to establish the ZAP connection */
    private static int http_port = 9090;
    /* The STRENGTH level used in the scanner */
    private static String scanner_strength = "High";
    /* The THRESHOLD level used in the scanner */
    private static String scanner_threshold = "Low";
    /* The RISK LEVEL minimum to be considered in the Alert List */
    private static String risk_level = "MEDIUM";
    //-------------------------------------------------------------------------------
    private static HashMap<String, String> attack_codes = new HashMap<>();
    private static ClientApi clientApi;
    private static String previousUrlScanned = "";
    private static HashMap<String, List<Alert>> hashMapScannedAlertsFound;

    private PenetrationTestingService() {
    }

    //<editor-fold desc="GETTER AND SETTERS">
    public static String getHttp_ip() {
        return http_ip;
    }

    public static void setHttp_ip(String http_ip) {
        PenetrationTestingService.http_ip = http_ip;
    }

    public static int getHttp_port() {
        return http_port;
    }

    public static void setHttp_port(int http_port) {
        PenetrationTestingService.http_port = http_port;
    }

    public static String getScanner_strength() {
        return scanner_strength;
    }

    public static void setScanner_strength(String scanner_strength) {
        PenetrationTestingService.scanner_strength = scanner_strength;
    }

    public static String getScanner_threshold() {
        return scanner_threshold;
    }

    public static void setScanner_threshold(String scanner_threshold) {
        PenetrationTestingService.scanner_threshold = scanner_threshold;
    }

    public static String getRisk_level() {
        return risk_level;
    }

    public static void setRisk_level(String risk_level) {
        PenetrationTestingService.risk_level = risk_level;
    }
    //</editor-fold>

    /**
     * @param urlToScan Is the url to be scanned
     * @return Returns a Map with an Alert List for each scan type (1. Passive Scan, 2. Active Scan, 3. Spider Scan)
     */
    public static HashMap<String, List<Alert>> runScanner(String urlToScan) {
        //Instances the "clientApi" with the previously configured IP and PORT
        clientApi = new ClientApi(http_ip, http_port);
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
        attack_codes.forEach((attackType, attackTypeId) ->
                runActiveScan(urlToScan, attackType, attackTypeId)
        );
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
        } catch (ClientApiException psEx) {
            LOGGER.error("Passive Scan \"ClientApiException\" Error: {}", psEx.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        } catch (InterruptedException psEx) {
            LOGGER.error("Passive Scan \"InterruptedException\" Error: {}", psEx.getMessage());
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
                clientApi.ascan.setScannerAttackStrength(id, scanner_strength, null);
                clientApi.ascan.setScannerAlertThreshold(id, scanner_threshold, null);
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
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("ACTIVE SCAN | " + scanTypeName);
        } catch (ClientApiException asEx) {
            LOGGER.error("Active Scan \"ClientApiException\" Error: {}", asEx.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        } catch (InterruptedException asEx) {
            LOGGER.error("Active Scan \"InterruptedException\" Error: {}", asEx.getMessage());
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
            }
            LOGGER.info("Spider Scan Completed in {} seconds", scanTime);
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("SPIDER SCAN");
        } catch (ClientApiException ssEx) {
            LOGGER.error("Spider Scan \"ClientApiException\" Error: {}", ssEx.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        } catch (InterruptedException ssEx) {
            LOGGER.error("Spider Scan \"InterruptedException\" Error: {}", ssEx.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
            Thread.currentThread().interrupt();
        }
    }

    private static void updateMapSecurityAlertList(String mapScanType) {
        //Get alert List to shown in the LOG
        List<Alert> lstAlerts = new ArrayList<>();
        //By default get "HIGH" risks only
        Alert.Risk risk = Alert.Risk.High;
        if ("MEDIUM".equalsIgnoreCase(risk_level)) {
            risk = Alert.Risk.Medium;
        } else if ("LOW".equalsIgnoreCase(risk_level)) {
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
        attack_codes = new HashMap<>();
        attack_codes.put("DIRECTORY_BROWSING", "0");
        attack_codes.put("PATH_TRAVERSAL", "6");
        attack_codes.put("REMOTE_FILE_INCLUSION", "7");
        attack_codes.put("SOURCE_CODE_DISCLOSURE", "10045");
        attack_codes.put("REMOTE_CODE_EXECUTION", "20018");
        attack_codes.put("EXTERNAL_REDIRECT", "20019");
        attack_codes.put("BUFFER_OVERFLOW", "30001");
        attack_codes.put("FORMAT_STRING_ERROR", "30002");
        attack_codes.put("CRLF_INJECTION", "40003");
        attack_codes.put("PARAMETER_TAMPERING", "40008");
        attack_codes.put("SERVER_SIDE_INCLUDE", "40009");
        attack_codes.put("CROSS_SITE_SCRIPTING", "40012,40014,40016,40017");
        attack_codes.put("SQL_INJECTION", "40018");
        attack_codes.put("SCRIPT_ACTIVE_SCAN_RULES", "50000");
        attack_codes.put("SERVER_SIDE_CODE_INJECTION", "90019");
        attack_codes.put("REMOTE_OS_COMMAND_INJECTION", "90020");
    }
}