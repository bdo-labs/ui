(ns ui.elements
  (:require [ui.element.button.views :as button]
            [ui.element.link.views :as link]
            [ui.element.icon.views :as icon]
            [ui.element.containers.views :as containers]
            [ui.element.content.views :as content]
            [ui.element.chooser.views :as chooser]
            [ui.element.progress-bar.views :as progress-bar]
            [ui.element.loaders.views :as loaders]
            [ui.element.textfield.views :as textfield]
            [ui.element.collection.views :as collection]
            [ui.element.label.views :as label]
            [ui.element.checkbox.views :as checkbox]
            [ui.element.calendar.views :as calendar]
            [ui.element.date-picker.views :as date-picker]
            [ui.element.period-picker.views :as period-picker]
            [ui.element.toggle.views :as toggle]
            [ui.element.modal.views :as modal]
            [ui.element.menu.views :as menu]
            [ui.element.color-swatch.views :as color-swatch]
            [ui.element.color-picker.views :as color-picker]
            [ui.element.numbers.views :as numbers]
            [ui.element.badge.views :as badge]))

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
