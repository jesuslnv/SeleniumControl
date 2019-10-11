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
    public static String HTTP_IP = "127.0.0.1";
    /* The PORT to establish the ZAP connection */
    public static int HTTP_PORT = 9090;
    /* The STRENGTH level used in the scanner */
    public static String SCANNER_STRENGTH = "High";
    /* The THRESHOLD level used in the scanner */
    public static String SCANNER_THRESHOLD = "Low";
    /* The RISK LEVEL minimum to be considered in the Alert List */
    public static String RISK_LEVEL = "MEDIUM";
    //-------------------------------------------------------------------------------
    private static HashMap<String, String> ATTACK_CODES = new HashMap<>() {{
        put("DIRECTORY_BROWSING", "0");
        put("PATH_TRAVERSAL", "6");
        put("REMOTE_FILE_INCLUSION", "7");
        put("SOURCE_CODE_DISCLOSURE", "10045");
        put("REMOTE_CODE_EXECUTION", "20018");
        put("EXTERNAL_REDIRECT", "20019");
        put("BUFFER_OVERFLOW", "30001");
        put("FORMAT_STRING_ERROR", "30002");
        put("CRLF_INJECTION", "40003");
        put("PARAMETER_TAMPERING", "40008");
        put("SERVER_SIDE_INCLUDE", "40009");
        put("CROSS_SITE_SCRIPTING", "40012,40014,40016,40017");
        put("SQL_INJECTION", "40018");
        put("SCRIPT_ACTIVE_SCAN_RULES", "50000");
        put("SERVER_SIDE_CODE_INJECTION", "90019");
        put("REMOTE_OS_COMMAND_INJECTION", "90020");
        //put("REMOTE_CODE_EXECUTION","20018");
        //put("LDAP_INJECTION","40015");
        //put("INSECURE_HTTP_METHODS","90028");
        //put("XPATH_INJECTION","90021");
        //put("PADDING_ORACLE","90024");
        //put("SHELL_SHOCK","10048");
        //put("XML_EXTERNAL_ENTITY","90023");
        //put("PARAMETER_POLLUTION","20014");
        //put("EL_INJECTION","90025");
        //put("PADDING_ORACLE","90024");
    }};
    private static ClientApi clientApi = new ClientApi(HTTP_IP, HTTP_PORT);
    private static String previousUrlScanned = "";
    private static HashMap<String, List<Alert>> hashMapScannedAlertsFound;

    /**
     * @param urlToScan Is the url to be scanned
     * @return Returns a Map with an Alert List for each scan type (1. Passive Scan, 2. Active Scan, 3. Spider Scan)
     */
    public static HashMap<String, List<Alert>> runScanner(String urlToScan) {
        hashMapScannedAlertsFound = new HashMap<>();
        //Verify if the "urlToScan" is equals to the "previousUrlScanned" to avoid multiple scans to the same URL.
        if (urlToScan.equals(previousUrlScanned)) {
            return null;
        }
        //Run Passive Scan First (Because is the most basic and simple Scan)
        runPassiveScan();
        //Run Active Scan with each specified Penetration Test
        ATTACK_CODES.forEach((attackType, attackTypeId) -> {
            runActiveScan(urlToScan, attackType, attackTypeId);
        });
        //Run Spider Scan at Last
        runSpiderScan(urlToScan);
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
            LOGGER.info("Passive Scan Completed in " + scanTime + " seconds");
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to update the AlertList based on riskLevel settled
            updateMapSecurityAlertList("PASSIVE SCAN");
        } catch (ClientApiException | InterruptedException ex) {
            LOGGER.error("Passive Scan Error: " + ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        }
    }

    private static void runActiveScan(String urlToScan, String scanTypeName, String scanTypeId) {
        LOGGER.info("--------------------------Starting Active Scan---------------------------");
        LOGGER.info("Scanning URL: " + urlToScan);
        LOGGER.info("Scan Type: " + scanTypeName);
        LOGGER.info("Scan Id: " + scanTypeId);
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
            LOGGER.info("Active Scan Completed in " + scanTime + " seconds");
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to display the Alerts based on riskLevel settled
            updateMapSecurityAlertList("ACTIVE SCAN | " + scanTypeName);
        } catch (ClientApiException | InterruptedException ex) {
            LOGGER.error("Active Scan Error: " + ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
        }
    }

    private static void runSpiderScan(String urlToScan) {
        LOGGER.info("--------------------------Starting Spider Scan---------------------------");
        LOGGER.info("Scanning URL: " + urlToScan);
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
            LOGGER.info("Spider Scan Completed in " + scanTime + " seconds");
            LOGGER.info("-------------------------------------------------------------------------");
            //Call the function to display the Alerts based on riskLevel settled
            updateMapSecurityAlertList("SPIDER SCAN");
        } catch (ClientApiException | InterruptedException ex) {
            LOGGER.error("Spider Scan Error: " + ex.getMessage());
            LOGGER.info("-------------------------------------------------------------------------");
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
            LOGGER.error(ex.getMessage());
        }
        //Add Alert List Found to the Map
        hashMapScannedAlertsFound.put(mapScanType, lstAlerts);
    }
}