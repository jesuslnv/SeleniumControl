[![Stargazers][stars-shield]][stars-url]
[![Travis][travis-shield]][travis-url]
[![Sonar][sonar-shield]][sonar-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

# Selenium Control

This is a base project to use as "Base Library" in your main Selenium Projects which simplifies the declaration and use of web components.

# Getting Started

This project doesn't run anything by itself but you can compile and use it as a base library to call different components in your Selenium Web Testing:
* [Components](#Components)
    * [Page](#Page)
    * [Control](#Control)
      1. [ButtonControl](#a-buttoncontrol)
      2. [SelectControl](#b-SelectControl)
      3. [TextControl](#c-TextControl)
* [Services](#Services)
    * [DateService](#DateService)
    * [ParameterService](#ParameterService)
    * [PenetrationTestingService](#PenetrationTestingService)

# Components
There are different components two main components declared in the project:

## Page
The Page component contains all the functions related to the **WebSite** that allows you to navigate it.

|Function Name                      |Action Perfomed|
|---                                |---|
|switchToTab                        |Switch To Specific Tab using Name|
|switchToLastTab                    |Switch To Last Tab|
|fileDownloadedCorrectly            |Verifies if the File is Correctly Downloaded|
|waitForModal                       |Wait for Modal to disappear|
|waitForPageLoad                    |Wait for Page Load|

## Control
The Control component contains all the functions to manipulate all the existing elements in the **WebSite**.

|Function Name                      |Action Perfomed|
|---                                |---|
|setxPosition                       |Moves the view in axis "X"|
|setyPosition                       |Moves the view in axis "Y"|
|isControlExist                     |Validates if the Control exist in the view|
|dragAndDrop                        |Allows to Drag and Drop elements in the view |
|mouseHover                         |Locates the mouse in specified Xpath|

There is a list of different controls to be used: 

#### A. ButtonControl
Is a control for manipulate any **Button** or **Link** in the **WebSite**.

#### B. SelectControl
Is a control for manipulate any **Select** in the **WebSite**.

#### C. TextControl
Is a control for manipulate any **Text** in the **WebSite**.

# Services
There are different necessary services to manipulate variables in Selenium and also to run tests that don't belong to Selenium by default.

## DateService
This service helps to interact with a Date and perform different operations

|Function Name                      |Action Perfomed|
|---                                |---|
|getDateTimeFormat                  |Returns the Date formatted as String|
|getDateOffset                      |Returns the Date formatted as String with the amount of days added|
|addMinutes                         |Returns the Date modified in Minutes as Date|

## ParameterService
This service allows you to store or manipulate parameters

|Function Name                      |Action Perfomed|
|---                                |---|
|setParameter                       |Sets a parameter to be stored|
|getParameter                       |Returns the value of a previously stored parameter|
|encryptString                      |Used to encrypt a String|
|decryptString                      |Used to decrypt a String|

## PenetrationTestingService
This service allows you to run a scanner that connects directly to a running **OWASP ZAP** instance and execute different tests to detect different security vulnerabilities.
The performed Scan includes **Passive Scan**, **Active Scan** and **Spider Scan**.

|Predefined Vars                    |Default Value          | Description |
|---                                |---                    |---          |
|httpIp                             |"127.0.0.1"            | Defines the IP where the OwaspZap is running|
|httpPort                           |9090                   | Defines the PORT where the OwaspZap is using|
|scannerStrength                    |"High"                 | Defines the Scanner Strength to be considered in all the Scans|
|scannerThreshold                   |"Low"                  | Defines the Scanner Threshold to be considered in all the Scans|
|reportFileLocation                 |"target/zapReport/"    | Defines the location for the report File|
|reportFileName                     |"report.json"          | Defines the report File Name|
|enablePassiveScan                  |true                   | Enables the Passive Scanner (True = Enabled, False = Disabled)|
|enableActiveScan                   |true                   | Enables the Active Scanner (True = Enabled, False = Disabled)|
|enableSpiderScan                   |true                   | Enables the Spider Scanner (True = Enabled, False = Disabled)|

|Function Name                      |Action Perfomed|
|---                                |---|
|runScanner                         |Runs the main scanner that includes all the defined scans, and it generates a JSON file report |

<!-- LINKS -->
[stars-shield]: https://img.shields.io/github/stars/jesuslnv/SeleniumControl.svg
[stars-url]: https://github.com/jesuslnv/SeleniumControl/stargazers
[travis-shield]: https://travis-ci.org/jesuslnv/SeleniumControl.svg?branch=master
[travis-url]: https://travis-ci.org/jesuslnv/SeleniumControl
[sonar-shield]: https://sonarcloud.io/api/project_badges/measure?project=jesuslnv_SeleniumControl&metric=alert_status
[sonar-url]: https://sonarcloud.io/dashboard?id=jesuslnv_SeleniumControl
[license-shield]: https://img.shields.io/github/license/jesuslnv/SeleniumControl.svg
[license-url]: https://github.com/jesuslnv/SeleniumControl/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?logo=linkedin&colorB=1E5799
[linkedin-url]: https://pe.linkedin.com/in/jesus-luis-neira-vizcarra-27b4b31a