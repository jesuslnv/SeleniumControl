[![Stargazers][stars-shield]][stars-url]
[![Travis][travis-shield]][travis-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

# Selenium Control

This is a base project to use as "Base Library" in your main Selenium Projects which simplifies the declaration and use of web components.

# Getting Started

This project doesn't run anything by itself but you can compile and use it as a base library to call different components in your Selenium Web Testing:
* [Components](#Components)
    * [Page](#Page)
    * [Control](#Control)
      * [ButtonControl](#ButtonControl)
      * [SelectControl](#SelectControl)
      * [TextControl](#TextControl)
* [Services](#Services)
    * [DateService](#DateService)
    * [ParameterService](#ParameterService)
    * [PenetrationTesting](#PenetrationTesting)

# Components
There are different components two main components declared in the project:

## Page
The Page component contains all the functions related to the **WebSite** that allows you to navigate it.

|Function Name                      |Action Perfomed|
|---                                |---|
|switchToTab                        |Switch To Specific Tab using Name|
|switchToLastTab                    |Switch To Last Tab|
|verify_FileDownloadedCorrectly     |Verify File Downloaded Correctly|
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

- [x] "setxPosition" Moves the view in axis "X"
- [x] "setyPosition" Moves the view in axis "Y"
- [x] "isControlExist" Validates if the Control exist in the view
- [x] "dragAndDrop" Allows to Drag and Drop elements in the view 
- [x] "mouseHover" Locates the mouse in specified Xpath

### 1. ButtonControl
- "setWaitForClick" Set time in seconds to wait until for do a Click
- "setAutoScroll" Set the AutoScroll enabled or disabled (True or False)
- "click" The event to perform a Click on element
- "rightClick" The event to perform a Right Click on element
- "doubleClick" The event to perform a Double Click on element

<!-- LINKS -->
[stars-shield]: https://img.shields.io/github/stars/jesuslnv/SeleniumControl.svg
[stars-url]: https://github.com/jesuslnv/SeleniumControl/stargazers
[travis-shield]: https://travis-ci.org/jesuslnv/SeleniumControl.svg?branch=master
[travis-url]: https://travis-ci.org/jesuslnv/SeleniumControl
[license-shield]: https://img.shields.io/github/license/jesuslnv/SeleniumControl.svg
[license-url]: https://github.com/jesuslnv/SeleniumControl/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?logo=linkedin&colorB=1E5799
[linkedin-url]: https://pe.linkedin.com/in/jesus-luis-neira-vizcarra-27b4b31a