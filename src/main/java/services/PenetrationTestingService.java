package services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.clientapi.core.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PenetrationTestingService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String httpIp = "127.0.0.1";
    private static int httpPort = 9090;
    private static String scannerStrength = "High";
    private static String scannerThreshold = "Low";
    private static String reportFileLocation = "target/zapReport/";
    private static String reportFileName = "report";
    private static boolean enableHTMLReport = true;
    private static boolean enableJSONReport = true;
    private static boolean enablePassiveScan = true;
    private static boolean enableActiveScan = true;
    private static boolean enableSpiderScan = true;
    private static int spiderScanTimeOut = 30;
    private static String previousUrlScanned = "";
    //-------------------------------------------------------------------------------
    private static Map<String, String> attackCodes = new HashMap<>();
    private static ClientApi clientApi;

    private PenetrationTestingService() {
    }

    //<editor-fold desc="GETTER AND SETTERS">

    /**
     * @param httpIp Is the IP to establish the ZAP connection (Default: "127.0.0.1")
     */
    public static void setHttpIp(String httpIp) {
        PenetrationTestingService.httpIp = httpIp;
    }

    /**
     * @param httpPort Is the PORT to establish the ZAP connection (Default: 9090)
     */
    public static void setHttpPort(int httpPort) {
        PenetrationTestingService.httpPort = httpPort;
    }

    /**
     * @param scannerStrength Is he STRENGTH level used in the scanner (Default: "High")
     */
    public static void setScannerStrength(String scannerStrength) {
        PenetrationTestingService.scannerStrength = scannerStrength;
    }

    /**
     * @param scannerThreshold Is the THRESHOLD level used in the scanner (Default: "Low")
     */
    public static void setScannerThreshold(String scannerThreshold) {
        PenetrationTestingService.scannerThreshold = scannerThreshold;
    }

    /**
     * @param reportFileLocation Is the ReportFileLocation sets up where the report is created (Default: "target/zapReport/")
     */
    public static void setReportFileLocation(String reportFileLocation) {
        PenetrationTestingService.reportFileLocation = reportFileLocation;
    }

    /**
     * @param reportFileName Is the ReportFileName sets up the name of the file created (Default: "report.html")
     */
    public static void setReportFileName(String reportFileName) {
        PenetrationTestingService.reportFileName = reportFileName;
    }

    /**
     * @param enableHTMLReport Enables the creation of an HTML Report (Default: true)
     */
    public static void setEnableHTMLReport(boolean enableHTMLReport) {
        PenetrationTestingService.enableHTMLReport = enableHTMLReport;
    }

    /**
     * @param enableJSONReport Enables the creation of an JSON Report (Default: true)
     */
    public static void setEnableJSONReport(boolean enableJSONReport) {
        PenetrationTestingService.enableJSONReport = enableJSONReport;
    }

    /**
     * @param enablePassiveScan Enables the Passive Scan (Default: true)
     */
    public static void setEnablePassiveScan(boolean enablePassiveScan) {
        PenetrationTestingService.enablePassiveScan = enablePassiveScan;
    }

    /**
     * @param enableActiveScan Enables the Active Scan (Default: true)
     */
    public static void setEnableActiveScan(boolean enableActiveScan) {
        PenetrationTestingService.enableActiveScan = enableActiveScan;
    }

    /**
     * @param enableSpiderScan Enables the Spider Scan (Default: true)
     */
    public static void setEnableSpiderScan(boolean enableSpiderScan) {
        PenetrationTestingService.enableSpiderScan = enableSpiderScan;
    }

    /**
     * @param spiderScanTimeOut Set the Spider Scan TimeOut in minutes (Default: 30)
     */
    public static void setSpiderScanTimeOut(int spiderScanTimeOut) {
        PenetrationTestingService.spiderScanTimeOut = spiderScanTimeOut;
    }

    /**
     * @return returns the previous URL scanned stored in this var
     */
    public static String getPreviousUrlScanned() {
        return previousUrlScanned;
    }
    //</editor-fold>

    /**
     * @param urlToScan Is the url to be scanned
     *                  It generates a HTML Report in a predefined location
     *                  If there are a previous report file with the same name, the function replaces the previous report with a new one
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
        //Call the function to generate the HTML Report
        if (enableHTMLReport) {
            try {
                byte[] bytes = clientApi.core.htmlreport();
                generateFileReport(bytes, ".html");
            } catch (ClientApiException ex) {
                LOGGER.error("Error getting the HTML Report: {}", ex.getMessage());
            }
        }
        //-----------------------------------------------------------------------------------------------------------
        //Call the function to generate the JSON Report
        if (enableJSONReport) {
            try {
                byte[] bytes = clientApi.core.jsonreport();
                generateFileReport(bytes, ".json");
            } catch (ClientApiException ex) {
                LOGGER.error("Error getting the JSON Report: {}", ex.getMessage());
            }
        }
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
            //Set the OWASP Zap Mode for the Passive Scan
            clientApi.core.setMode("standard");
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
            //Clear all Spider Scans
            clientApi.spider.removeAllScans();
            //Set all the Options in the Spider Scan
            clientApi.core.setMode("attack");
            clientApi.spider.setOptionThreadCount(50);
            clientApi.spider.setOptionMaxDepth(5);
            clientApi.spider.setOptionMaxDuration(spiderScanTimeOut);
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
                progress = Integer.parseInt(((ApiResponseElement) clientApi.spider.status(scanId)).getValue());
                //After "300" seconds if the scan stay stuck in 99% it cancels the current Scan.
                if (progress == 99) {
                    progressStuck++;
                }
                //After "300" seconds the "Spider Scan" is automatically stopped to avoid a permanent Stuck bug
                if (progressStuck >= 300) {
                    //Stop and remove all Scans
                    clientApi.spider.stopAllScans();
                    clientApi.spider.removeAllScans();
                    //Generates an Exception due a possible stuck
                    throw new InterruptedException("Spider Scan stuck timeout " + progressStuck + " seconds.");
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

    private static void generateFileReport(byte[] bytes, String extension) {
        //Get alert List to shown in the LOG
        FileWriter fileWriter = null;
        try {
            String stringFile = new String(bytes, StandardCharsets.UTF_8);
            //Validates if the File Location don't exists, is created
            File fileValidator = new File(reportFileLocation);
            if (!fileValidator.exists()) {
                fileValidator.mkdirs();
            }
            //Creates the report file
            File reportFile = new File(reportFileLocation + reportFileName + extension);
            fileWriter = new FileWriter(reportFile);
            fileWriter.write(stringFile);
        } catch (IOException ex) {
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