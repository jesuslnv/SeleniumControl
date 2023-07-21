[![Stargazers][stars-shield]][stars-url]
[![Travis][travis-shield]][travis-url]
[![Sonar][sonar-shield]][sonar-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

# Selenium Control

This is a base project to use as "Base Library" in your main Selenium Projects which simplifies the declaration and use
of web components.

# Getting Started

This project doesn't run anything by itself but you can compile and use it as a base library to call different
components in your Selenium Web Testing:

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

|Function Name                      |Action Performed|
|---                                |---|
|switchToTab                        |Switch To Specific Tab using Name|
|switchToLastTab                    |Switch To Last Tab|
|fileDownloadedCorrectly            |Verifies if the File is Correctly Downloaded|
|waitForModal                       |Wait for Modal to disappear|
|waitForPageLoad                    |Wait for Page Load|

## Control

The Control component contains all the functions to manipulate all the existing elements in the **WebSite**.

|Function Name                      |Action Performed|
|---                                |---|
|setxPosition                       |Moves the view in axis "X"|
|setyPosition                       |Moves the view in axis "Y"|
|isControlExist                     |Validates if the Control exist in the view|
|dragAndDrop                        |Allows to Drag and Drop elements in the view |
|mouseHover                         |Locates the mouse in specified Xpath|
|sendkeyToElement                   |Sends a keystroke to the predefined Element by Xpath|

There is a list of different controls to be used:

#### A. ButtonControl

Is a control for manipulate any **Button** or **Link** in the **WebSite**.

|Function Name                      |Action Performed|
|---                                |---|
|setWaitForClick                    |Defines the time to wait before click on element|
|setAutoScroll                      |Enables the option to auto scroll the view to the element (Default: true)|
|click                              |Allows to "Click" on specified Element|
|rightClick                         |Allows to "Right Click" on specified Button Element|
|doubleClick                        |Allows to "Double Click" on specified Button Element|

#### B. SelectControl

Is a control for manipulate any **Select** in the **WebSite**.

|Function Name                      |Action Performed|
|---                                |---|
|setWaitForClick                    |Defines the time to wait before click on element|
|setAutoScroll                      |Enables the option to auto scroll the view to the element (Default: true)|
|selectElement                      |Allows to select the value inside a Select Element (Works with a select HTML component)|
|selectButtonElement                |Allows to select the specified item inside a Select Element|
|selectCheckBox                     |Allows to check multiple items inside a Select Element|

#### C. TextControl

Is a control for manipulate any **Text** in the **WebSite**.

|Function Name                      |Action Performed|
|---                                |---|
|setWaitForClick                    |Defines the time to wait before click on element|
|setAutoScroll                      |Enables the option to auto scroll the view to the element (Default: true)|
|setText                            |Allows to write the value inside a Text Element|
|setTextAutoComplete                |Allows to select the specified item displayed after a Text Autocomplete Element|
|getContainedText                   |Returns the text contained by the specified Element|

# Services

There are different necessary services to manipulate variables in Selenium and also to run tests that don't belong to
Selenium by default.

## DateService

This service helps to interact with a Date and perform different operations

|Function Name                      |Action Performed|
|---                                |---|
|getDateTimeFormat                  |Returns the Date formatted as String|
|getDateOffset                      |Returns the Date formatted as String with the amount of days added|
|addMinutes                         |Returns the Date modified in Minutes as Date|

## ParameterService

This service allows you to store or manipulate parameters

|Function Name                      |Action Performed|
|---                                |---|
|setParameter                       |Sets a parameter to be stored|
|getParameter                       |Returns the value of a previously stored parameter|
|encryptString                      |Used to encrypt a String|
|decryptString                      |Used to decrypt a String|
|requestGetService                  |Used to return a response from a URL |
|requestPostService                 |Used to return a response from a URL|
|requestPutService                  |Used to return a response from a URL |

## Usage

Generate a <**jar**> file using the next command:

```sh
mvn clean -U package
```

When you have the <**jar**> file ready, you can import it in your project to call the scanner.

---
To install your library to use it as a normal dependency, you should execute this command:

```sh
mvn clean -U install
```

In case you executed the previous command, you can call the dependency putting this dependency rule in your **POM.xml**
file.

```xml
<dependency>
    <groupId>SeleniumControl</groupId>
    <artifactId>SeleniumControl</artifactId>
    <version>1.0</version>
</dependency>
```

When your project library is imported the next step is call an element in your main project referencing the proper **
XPath**.

- Example to use a **ButtonControl** java implementation:

```bash
ButtonControl buttonControl=new ButtonControl(webDriver,"XPATH");
buttonControl.click();
```

- Example to use a **SelectControl** java implementation:

```bash
SelectControl selectControl=new ButtonControl(webDriver,"XPATH");
selectControl.isControlExist();
```

- Example to use a **TextControl** java implementation:

```bash
TextControl textControl=new TextControl(webDriver,"XPATH");
textControl.setText(user);
```

When you use any of the mentioned implementation, is important to initialize your **webDriver** parameter and set the
proper **XPath** in each operation because each one is different for each call.
Each component have a internal function specified at the beginning of this documentation.

## Project Example

Here is a [sample](https://github.com/jesuslnv/Infrastructure) project that you can download and run on your PC to test
this library.
The base documentation for this project is in the GitHub Readme and inside the source code.

<!-- LINKS -->

[stars-shield]: https://img.shields.io/github/stars/jesuslnv/SeleniumControl.svg

[stars-url]: https://github.com/jesuslnv/SeleniumControl/stargazers

[travis-shield]: https://travis-ci.com/jesuslnv/SeleniumControl.svg?branch=master

[travis-url]: https://travis-ci.com/jesuslnv/SeleniumControl

[sonar-shield]: https://sonarcloud.io/api/project_badges/measure?project=jesuslnv_SeleniumControl&metric=alert_status

[sonar-url]: https://sonarcloud.io/dashboard?id=jesuslnv_SeleniumControl

[license-shield]: https://img.shields.io/badge/License-MIT-green.svg

[license-url]: https://github.com/jesuslnv/SeleniumControl/blob/master/LICENSE

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?logo=linkedin&colorB=1E5799

[linkedin-url]: https://pe.linkedin.com/in/jesus-luis-neira-vizcarra-27b4b31a