(ns ui.elements
  (:require [ui.element.badge.views :as badge]
            [ui.element.button.views :as button]
            [ui.element.calendar.views :as calendar]
            [ui.element.checkbox.views :as checkbox]
            [ui.element.chooser.views :as chooser]
            [ui.element.collection.views :as collection]
            [ui.element.color-picker.views :as color-picker]
            [ui.element.color-swatch.views :as color-swatch]
            [ui.element.containers.views :as containers]
            [ui.element.content.views :as content]
            [ui.element.date-picker.views :as date-picker]
            [ui.element.icon.views :as icon]
            [ui.element.label.views :as label]
            [ui.element.link.views :as link]
            [ui.element.loaders.views :as loaders]
            [ui.element.menu.views :as menu]
            [ui.element.modal.views :as modal]
            [ui.element.numberfield.views :as numberfield]
            [ui.element.numbers.views :as numbers]
            [ui.element.notification.views :as notification]
            [ui.element.period-picker.views :as period-picker]
            [ui.element.progress-bar.views :as progress-bar]
            [ui.element.sidebar.views :as sidebar]
            [ui.element.tabs.views :as tabs]
            [ui.element.textfield.views :as textfield]
            [ui.element.toggle.views :as toggle]))

;; Action Elements
(def dropdown menu/dropdown)
(def button button/button)
(def link link/link)
(def icon icon/icon)
(def color-swatch color-swatch/color-swatch)
(def color-picker color-picker/color-picker)

;; Layout Elements
(def container containers/container)
(def sidebar sidebar/sidebar)
(def header containers/header)
(def code containers/code)
(def card containers/card)

;; Content Elements
(def markdown content/markdown)
(def article content/article)
(def section content/section)
(def vr content/vr)
(def hr content/hr)
(def label label/label)
(def notification notification/notification)
(def notifications notification/notifications)
(def tabs tabs/tabs-)


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

;; Load-Indication Elements
(def progress-bar progress-bar/progress-bar)
(def spinner loaders/spinner)
