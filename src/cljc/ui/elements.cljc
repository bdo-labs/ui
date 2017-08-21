(ns ui.elements
  (:require [ui.element.button :as button]
            [ui.element.icon-button :as icon-button]
            [ui.element.link :as link]
            [ui.element.icon :as icon]
            [ui.element.containers :as containers]
            [ui.element.content :as content]
            [ui.element.auto-complete :as auto-complete]
            [ui.element.progress-bar :as progress-bar]
            [ui.element.loaders :as loaders]
            [ui.element.textfield :as textfield]
            [ui.element.boundary :as boundary]
            [ui.element.collection :as collection]
            [ui.element.label :as label]
            [ui.element.checkbox :as checkbox]
            [ui.element.calendar :as calendar]
            [ui.element.date-picker :as date-picker]
            [ui.element.toggle :as toggle]
            [ui.element.clamp :as clamp]
            [ui.element.modal :as modal]
            [ui.element.menu :as menu]
            ;; [ui.element.timeline :as timeline]
            [ui.element.color-swatch :as color-swatch]
            [ui.element.color-picker :as color-picker]
            [ui.element.numbers.views :as numbers]
            ))

(def dropdown menu/dropdown)


(def button button/button)
(def link link/link)
(def icon icon/icon)


(def color-swatch color-swatch/color-swatch)
(def color-picker color-picker/color-picker)


(def container containers/container)
(def sidebar containers/sidebar)
;(def card containers/card)
(def header containers/header)
(def code containers/code)


;; Content elements
(def markdown content/markdown)
(def article content/article)
(def section content/section)
(def vr content/vr)
(def hr content/hr)
(def label label/label)


(def sheet numbers/sheet)


;; Input elements
(def checkbox checkbox/checkbox)
(def toggle toggle/toggle)
(def textfield textfield/textfield)
(def auto-complete auto-complete/auto-complete)
(def clamp clamp/clamp)
(def days calendar/days)
(def months calendar/months)
(def years calendar/years)
(def date-picker date-picker/date-picker)
;(def timeline timeline/timeline)


(def dialog modal/dialog)
(def confirm-dialog modal/confirm-dialog)

(def progress-bar progress-bar/progress-bar)
(def spinner loaders/spinner)



;; Virtuals
(def boundary boundary/boundary)

