Planetarium for Samsung Gear VR
===============================

This is a simple (but not too simple) planetarium app for Samsung Gear VR!
By Dmitry Brant, Wikimedia Foundation.

Features:
* All stars in the night sky brighter than magnitude 4.5.
* Textured and correctly-positioned planets (with exaggerated size for easier selection).
* Small selection of Messier objects.
* Hover over an object to see its name.
* Tap on the VR trackpad when hovering over an object to see a detailed description. (Press the Back button or tap in empty space to dismiss the description)
* Scroll horizontally on the VR trackpad to shift field of view.

To do:
* Proper lighting from the Sun, to create correct phases of Moon and planets.
* Rings of Saturn.
* Moons of other planets.
* Constellation lines / borders.
* Milky Way background.
* More Messier objects.

Notes
-----

This project contains pre-built binaries from the Oculus SDK and the Samsung Gear VR Framework (the framework still uses Eclipse instead of Gradle). To update, use these resources:
* https://github.com/Samsung/GearVRf/releases
* https://developer.oculus.com/downloads/mobile/0.6.2.0/Oculus_Mobile_SDK/

This project uses Wikimedia's RESTBase API for retrieving article content: http://rest.wikimedia.org/en.wikipedia.org/v1/?doc

This project uses the AstroLib library (http://mhuss.com/AstroLib/) for calculating planet positions.

License
-------

    Copyright 2015 Dmitry Brant

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
