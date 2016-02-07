Planetarium for Samsung Gear VR
===============================

[![Build Status](https://travis-ci.org/dbrant/GearVR_planetarium.svg)](https://travis-ci.org/dbrant/GearVR_planetarium)

A basic (but evolving) virtual planetarium app for Samsung Gear VR!

Features:
* All stars in the night sky brighter than magnitude 4.5.
* Constellations with name labels.
* Textured and correctly-positioned planets (with exaggerated size for easier selection).
* Small selection of Messier objects.
* Hover over an object to see its name.
* Tap on the VR trackpad when hovering over an object to see a detailed description from Wikipedia. (Press the Back button or tap in empty space to dismiss the description)
* Scroll horizontally on the VR trackpad to shift field of view.

To do:
* Proper lighting from the Sun, to create correct phases of Moon and planets.
* Rings on Neptune.
* Moons of other planets.
* Tilt of other planets.
* Milky Way background.
* More Messier objects.

Running on your device
----------------------

To run the app on your device, you'll need to generate an Oculus Signature File for the device, and put the file in the "assets" directory.
https://developer.oculus.com/osig/

You may then build and run the app in the usual way.

Notes
-----

This project contains pre-built binaries from the Oculus SDK and the Samsung Gear VR Framework (the framework still uses Eclipse instead of Gradle). To update, use these resources:
* https://github.com/Samsung/GearVRf/releases
* https://developer.oculus.com/downloads/mobile/1.0.0.0/Oculus_Mobile_SDK/

This project uses Wikimedia's RESTBase API for retrieving article content: http://rest.wikimedia.org/en.wikipedia.org/v1/?doc

This project uses the AstroLib library (http://mhuss.com/AstroLib/) for calculating planet positions.

License
-------

    Copyright (c) 2015-2016 Dmitry Brant

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
