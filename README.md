# [ **ui** \"0.0.1\" ] 

## A Straight-Forward Library for Composing User-Interfaces


### Introduction

At the moment, `ui` is geared towards Clojure-projects that use [Reagent](//reagent-project.github.io/) on the front-end. 

**Please note:** This library is not considered stable.

Feeling adventurous? Grab the the [latest release](//github.com/bdo-labs/ui/releases/latest/) and experiment! 


### Project anatomy

There are four verticals to `ui`. Layout, elements, virtuals and wires. Layout and elements are quite self-explanatory in their naming. Additionally, there is virtuals. Virtuals are mostly used in the same manner as regular elements, but instead of adding nodes to the DOM, they'll add functionality to existing nodes. Wires are a bunch of initialization, events and subscriptions that makes ui easier to work with.


### Contributing

To contribute you'll need to compile things first. Below is a short outline on how to get setup properly. We also have some [contribution-guidelines](./.github/CONTRIBUTING.md) that you should have in mind. 


### Pre-Requisits

- [clojure 1.9.0](https://clojure.org/community/downloads#_stable_release_1_9_0_dec_8_2017)
- [boot-clj](http://boot-clj.com/)
- [node](https://nodejs.org/en/download/current/)


### Building

To build `ui`, simply issue the command `boot dev -s`. That will set the wheels in motion and leave you with an interactive build environment. The first time it will also grab some JavaScript-dependencies from `npm`, so be very patient.  

Once installed and running, you'll be asked whether you'd like to open up a browser with the URL (http://localhost:3000/). Answer [y]es, and that's it.. Good luck!
