package services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.clientapi.core.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    /* The ReportFileLocation sets up where the report is created */
    private static String reportFileLocation = "target/zapReport/";
    /* The ReportFileName sets up the name of the file created */
    private static String reportFileName = "report.html";
    /* Enables specific Scanners (Passive Scan, Active Scan, Spider Scan) */
    private static boolean enablePassiveScan = true;
    private static boolean enableActiveScan = true;
    private static boolean enableSpiderScan = true;
    //-------------------------------------------------------------------------------
    private static Map<String, String> attackCodes = new HashMap<>();
    private static ClientApi clientApi;
    private static String previousUrlScanned = "";

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

    public static String getReportFileLocation() {
        return reportFileLocation;
    }

    public static void setReportFileLocation(String reportFileLocation) {
        PenetrationTestingService.reportFileLocation = reportFileLocation;
    }

    public static String getReportFileName() {
        return reportFileName;
    }

    public static void setReportFileName(String reportFileName) {
        PenetrationTestingService.reportFileName = reportFileName;
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

    public static boolean isEnableSpiderScan() {
        return enableSpiderScan;
    }

    public static void setEnableSpiderScan(boolean enableSpiderScan) {
        PenetrationTestingService.enableSpiderScan = enableSpiderScan;
    }
    //</editor-fold>

    /**
     * @param urlToScan Is the url to be scanned
     */
    public static void runScanner(String urlToScan) {
        //-----------------------------------------------------------------------------------------------------------
        //Instances the "clientApi" with the previously configured IP and PORT
        clientApi = new ClientApi(httpIp, httpPort);
        //-----------------------------------------------------------------------------------------------------------
        try {
            //Remove all Historical Alerts stored
            clientApi.alert.deleteAllAlerts();
        } catch (ClientApiException ex) {
            LOGGER.error("Error removing Alerts: {}", ex.getMessage());
        }
        //-----------------------------------------------------------------------------------------------------------
        //Verify if the "urlToScan" is equals to the "previousUrlScanned" to avoid multiple scans to the same URL.
        if (urlToScan.equals(previousUrlScanned)) {
            return;
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
        //Run Spider Scan at Last
        if (enableSpiderScan) {
            runSpiderScan(urlToScan);
        }
        //-----------------------------------------------------------------------------------------------------------
        //Call the function to generate the File Report
        generateFileReport();
        //-----------------------------------------------------------------------------------------------------------
        //Set the current URL scanned to the previous
        previousUrlScanned = urlToScan;
    }

    private static void runPassiveScan() {
        LOGGER.info("--------------------------Starting Passive Scan--------------------------");
        try {
            //-----------------------------------------------------------------------------------------------------------
            //Enable all Scanners in Passive Mode
            clientApi.pscan.enableAllScanners();
            //-----------------------------------------------------------------------------------------------------------
            //Change all Passive Scans Threshold
            configurePassiveScanThreshold();
            //-----------------------------------------------------------------------------------------------------------
            //Start the Passive Scanner
            ApiResponse apiResponse = clientApi.pscan.recordsToScan();
            int scanTime = 0;
            while (!apiResponse.toString().equals("0")) {
                Thread.sleep(1000);
                scanTime++;
                apiResponse = clientApi.pscan.recordsToScan();
            }
            LOGGER.info("Passive Scan Completed in {} seconds", scanTime);
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
            //-----------------------------------------------------------------------------------------------------------
            //Disable all other Scanners by Default
            clientApi.ascan.disableAllScanners(null);
            //-----------------------------------------------------------------------------------------------------------
            //Set Attack Mode in OwaspZap
            clientApi.core.setMode("attack");
            //-----------------------------------------------------------------------------------------------------------
            //Enable specific Active Scanner
            clientApi.ascan.enableScanners(scanTypeId, null);
            for (String id : scanTypeId.split(",")) {
                clientApi.ascan.setScannerAttackStrength(id, scannerStrength, null);
                clientApi.ascan.setScannerAlertThreshold(id, scannerThreshold, null);
            }
            //-----------------------------------------------------------------------------------------------------------
            //Start the Active Scanner
            ApiResponse apiResponse = clientApi.ascan.scan(urlToScan, "True", "False", "Default Policy", null, null);
            String scanId = ((ApiResponseElement) apiResponse).getValue();
            int progress = 0;
            int scanTime = 0;
            while (progress < 100) {
                Thread.sleep(1000);
                scanTime++;
                progress = Integer.parseInt(((ApiResponseElement) clientApi.ascan.status(scanId)).getValue());
            }
            LOGGER.info("Active Scan Completed in {} seconds", scanTime);
        } catch (ClientApiException asEx) {
            LOGGER.error("Active Scan \"ClientApiException\" Error: {}", asEx.getMessage());
        } catch (InterruptedException asEx) {
            LOGGER.error("Active Scan \"InterruptedException\" Error: {}", asEx.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("-------------------------------------------------------------------------");
        }
    }

    private static void runSpiderScan(String urlToScan) {
        LOGGER.info("--------------------------Starting Spider Scan---------------------------");
        LOGGER.info("Scanning URL: {}", urlToScan);
        try {
            //Enable specific Active Scanner
            ApiResponse apiResponse = clientApi.spider.scan(urlToScan, null, null, null, null);
            String scanId = ((ApiResponseElement) apiResponse).getValue();
            int progress = 0;
            int scanTime = 0;
            int progressStuck = 0;
            //Polling the status until it completes
            while (progress < 100) {
                Thread.sleep(1000);
                scanTime++;
                progressStuck++;
                progress = Integer.parseInt(((ApiResponseElement) clientApi.spider.status(scanId)).getValue());
                //After 90 seconds the "Spider Scan" is automatically stopped to avoid a permanent Stuck bug
                if (progressStuck >= 60) {
                    //Stop and remove all Scans
                    clientApi.spider.stopAllScans();
                    clientApi.spider.removeAllScans();
                    //Generates an Exception due a possible stuck
                    throw new InterruptedException("Spider Scan timeout " + progressStuck + " seconds.");
                }
            }
            LOGGER.info("Spider Scan Completed in {} seconds", scanTime);
        } catch (ClientApiException ssEx) {
            LOGGER.error("Spider Scan \"ClientApiException\" Error: {}", ssEx.getMessage());
        } catch (InterruptedException ssEx) {
            LOGGER.error("Spider Scan \"InterruptedException\" Error: {}", ssEx.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("-------------------------------------------------------------------------");
        }
    }

    private static void generateFileReport() {
        //Get alert List to shown in the LOG
        FileWriter fileWriter = null;
        try {
            byte[] bytes = clientApi.core.htmlreport();
            String stringFile = new String(bytes, StandardCharsets.UTF_8);
            //Validates if the File Location don't exists, is created
            File fileValidator = new File(reportFileLocation);
            if(!fileValidator.exists()){
                fileValidator.mkdirs();
            }
            //Creates the report file
            File reportFile = new File(reportFileLocation + reportFileName);
            fileWriter = new FileWriter(reportFile);
            fileWriter.write(stringFile);
        } catch (ClientApiException | IOException ex) {
            LOGGER.error("Error generating HTML report: {}", ex.getMessage());
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ex) {
                    LOGGER.error("Error closing the file: {}", ex.getMessage());
                }
            }
        }
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

    private static void configurePassiveScanThreshold() {
        //-----------------------------------------------------------------------------------------------------------
        //Change all Passive Scans Threshold one by one
        try {
            ApiResponseList apiResponseList = (ApiResponseList) clientApi.pscan.scanners();
            List<ApiResponse> lstApiResponse = apiResponseList.getItems();
            for (ApiResponse tmpApiResponse : lstApiResponse) {
                ApiResponseSet apiResponseSet = (ApiResponseSet) tmpApiResponse;
                String pScanId = apiResponseSet.getStringValue("id");
                clientApi.pscan.setScannerAlertThreshold(pScanId, scannerThreshold);
            }
        } catch (ClientApiException ex) {
            LOGGER.error("Error setting up Passive Scan Threshold: {}", ex.getMessage());
        }
        //-----------------------------------------------------------------------------------------------------------
    }
}