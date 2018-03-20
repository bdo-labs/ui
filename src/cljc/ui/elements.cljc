(ns ui.elements
  (:require [ui.element.badge :as badge]
            [ui.element.button :as button]
            [ui.element.calendar :as calendar]
            [ui.element.checkbox :as checkbox]
            [ui.element.chooser :as chooser]
            [ui.element.collection :as collection]
            [ui.element.color-picker :as color-picker]
            [ui.element.color-swatch :as color-swatch]
            [ui.element.containers :as containers]
            [ui.element.content :as content]
            [ui.element.date-picker :as date-picker]
            [ui.element.icon :as icon]
            [ui.element.label :as label]
            [ui.element.link :as link]
            [ui.element.loaders :as loaders]
            [ui.element.menu :as menu]
            [ui.element.modal :as modal]
            [ui.element.numberfield :as numberfield]
            [ui.element.numbers.views :as numbers]
            [ui.element.period-picker :as period-picker]
            [ui.element.progress-bar :as progress-bar]
            [ui.element.textfield :as textfield]
            [ui.element.toggle :as toggle]))

;; Action Elements
(def dropdown menu/dropdown)
(def button button/button)
(def link link/link)
(def icon icon/icon)
(def color-swatch color-swatch/color-swatch)
(def color-picker color-picker/color-picker)


;; Layout Elements
(def container containers/container)
(def sidebar containers/sidebar)
(def header containers/header)
(def code containers/code)
;; TODO card


;; Content Elements
(def markdown content/markdown)
(def article content/article)
(def section content/section)
(def vr content/vr)
(def hr content/hr)
(def label label/label)


;; Form Elements
(def sheet numbers/sheet)
(def checkbox checkbox/checkbox)
(def toggle toggle/toggle)
(def textfield textfield/textfield)
(def numberfield numberfield/numberfield)
(def chooser chooser/chooser)
(def collection collection/collection)
(def days calendar/days)
(def months calendar/months)
(def years calendar/years)
(def date-picker date-picker/date-picker)
(def period-picker period-picker/period-picker)


;; In your face Elements
(def dialog modal/dialog)
(def confirm-dialog modal/confirm-dialog)
(def badge badge/badge)
;; TODO notifications


;; Load-Indication Elements
(def progress-bar progress-bar/progress-bar)
(def spinner loaders/spinner)
