(ns ui.readme)

(def content "# [ **ui** \"0.0.1\" ] 

## A Straight-Forward Library for Composing User-Interfaces


### Introduction

At the moment, `ui` is geared towards Clojure-projects that  
use [Reagent](//reagent-project.github.io/) on the front-end. If your  
adventurous and would like to use `ui` with some other technology, you  
can download  
the [latest release](//github.com/bdo-labs/ui/releases/latest/) and  
experiment as you please. Guides will be available when the library is  
considered stable.  


### Project anatomy

There are four verticals to `ui`. Layout, elements, virtuals and
wires. Layout and elements are quite self-explanetory in their naming
and then you have virtuals. Virtuals are mostly used in the same
manner as regular elements, but instead of adding nodes to the DOM,
they will add functionality to existing nodes. Wires are a bunch of
initialization, events and subscriptions that makes ui easier to work
with.


### Contributing

To contribute code, you'll need to compile the ting, so below there's  
a short outline of how you might do that. We also have  
some [contribution-guidelines](./.github/CONTRIBUTING.md) that you  
should have in mind.  


### Pre-Requisits

- [clojure 1.9.0-alpha17](https://clojure.org/community/downloads#_development_release_1_9_0_alpha17_may_26_2017)
- [boot-clj](http://boot-clj.com/)
- [node](https://nodejs.org/en/download/current/)


### Building

To build `ui`, simply issue the command `boot dev -s`. That will set
the wheels in motion and leave you with an interactive build
environment. The first time it will also grab some
JavaScript-dependencies from `npm`, so be very patient.  Ones you hear
a *pling*, it should all be installed and running, you'll be asked
whether you'd like to open up a browser with the URL
(http://localhost:3000/). Answer [y]es, and that's it.. Good luck!
")