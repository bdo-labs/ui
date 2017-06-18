(ns ui.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes defstyles]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes defstyles]])
            [garden.units :as unit]
            [garden.color :as color]
            [garden.selectors :as selector :refer [defpseudoelement]]
            [ui.util :as u]
            [ui.element.boundary :as boundary]
            [ui.element.checkbox :as checkbox]
            [ui.element.clamp :as clamp]
            [ui.element.progress-bar :as progress-bar]))


;;
;; Realizations that should be materialized
;;
;; - Many layout-problems arise from the fact that we're mixing
;;   text-layouts with structure. I think we would be well
;;   served by resetting everything quite strictly, so that line-height
;;   etc are extremely uniform, then have a text-content box with all
;;   typographic styles applied. This is how it works on most other
;;   platforms; say "wonder why?" ;)
;;
;; - Styles should be moved closer to it's element, preferably in the
;;   same file as it's views. I think going even one step further and
;;   having most of it scoped to it's element could be useful. Knowing
;;   that those styles will not bleed into other areas would be super
;;   useful.
;;
;; - Animations should be made a first-class citizen of this library.
;;   Strategy for how containers will melt together or spread apart,
;;   how they push and pull each other and so forth. This also means
;;   that most elements will need to be created from the same cloth
;;   / "container".
;;


(defcssfn cubic-bezier)
(defcssfn linear-gradient)
(defcssfn rotateZ)
(defcssfn scale)
(defcssfn translateX)
(defcssfn translateY)
(defcssfn translateZ)


#_(defpseudoelement -webkit-color-swatch-wrapper)
#_(defpseudoelement -webkit-color-swatch)


(def theme
  {:default {:background (color/rgb [245 245 245])
             :primary (color/rgb [96 191 253])
             :secondary (color/rgb [90 100 110])
             :positive (color/rgb [34 192 100])
             :negative (color/rgb [232 83 73])
             :font-weight 100
             :font-scale (unit/rem 1.8)}})


(defn- golden [x] (* (/ x 16) 9))


(defn- structure [theme]
  [[:body {:overflow :hidden}]
   ;; TODO #app:first-child, really!?
   [#{:html :body :#app :#app:first-child} {:height (unit/percent 100)
                                            :width  (unit/percent 100)}]
   [#{:html :body :menu :ul} {:margin  0
                              :padding 0}]
   [:main {:height "calc(100vh - 64px)"}]
   [#{:.Vertical-rule :.Horizontal-rule} {:background-color :silver}]
   [:.Vertical-rule {:width        (unit/px 1)
                     :min-height   (unit/rem 2.5)
                     :position     :relative
                     :left         (unit/rem 3)
                     :margin-right (unit/rem 6)
                     :height       (unit/percent 100)}]
   [:.Horizontal-rule {:width         (unit/percent 100)
                       :height        (unit/px 1)
                       :position      :relative
                       :top           (unit/rem 3)
                       :margin-bottom (unit/rem 6)}]
   [:article {:padding    (unit/rem 4)
              :text-align :left}
    [:section {:max-width    (unit/rem 70)
               :margin-right (unit/rem 3)}]]])


(defn- layouts [{:keys [background primary]}]
  [[:.Backdrop {:background (color/rgba [0 0 1 0.5])
                :width      (unit/percent 100)
                :height     (unit/percent 100)
                :position   :absolute
                :left       0
                :top        0
                :transition [[:opacity :500ms :ease]]
                :opacity    0
                :z-index    -1}]
   [[:.Sidebar.Locked.Align-left :.Slider :main] {:margin-left (unit/px 360)}]
   [:.Sidebar {:overflow :hidden
               :width    (unit/percent 100)
               :height   (unit/percent 100)}
    [:.Slider {:width          (unit/percent 100)
               :height         (unit/percent 100)
               :position       :relative}]
    [:sidebar {:width       (unit/px 360)
               :background  background
               :height      (unit/percent 100)
               :position    :absolute
               :top 0
               :z-index     9
               :line-height (unit/rem 3)}]
    [:main {:width              (unit/percent 100)
            :overflow           :auto
            :overflow-scrolling :touch
            :position           :relative
            :height             (unit/percent 100)}]
    [:&.Locked
     [:main {:width "calc(100% - 360px)"}]]
    #_[(selector/& (selector/not :.Locked))
     [:.Slider {:transition [[:500ms :ease]]}]
     [:&.Align-left
      [:sidebar {:left (unit/px -360)}]]
     [:&.Align-right
      [:sidebar {:right (unit/px -360)}]]
     [:&.Ontop
      [:sidebar {:transition [[:500ms :ease]]}]]
     [:&.Open
      [:.Backdrop {:opacity 1
                   :z-index 8}]
      [(selector/& (selector/not :.Ontop))
       [:&.Align-left [:.Slider {:transform (translateX (unit/px 360))}]]
       [:&.Align-right [:.Slider {:transform (translateX (unit/px -360))}]]]
      [:&.Ontop
       [:sidebar {:box-shadow [[0 0 (unit/rem 6) (color/rgba [0 0 1 0.5])]]}]
       [:&.Align-left [:sidebar {:transform (translateX (unit/px 360))}]]
       [:&.Align-right [:sidebar {:transform (translateX (unit/px -360))}]]]]]]])


(defn- header [theme]
  [[#{:.Header :.Card} {:background-color :white
                     :box-shadow       [[0 (unit/rem 0.2) (unit/rem 0.3) (color/rgba [35 35 35 0.2])]]}]
   [:.Header {:justify-content :space-between
              :align-items     :center
              :width           (unit/vw 100)
              :box-sizing      :border-box
              :padding         [[0 (unit/rem 1)]]
              :position        :relative}
    [:&.Small {:flex    [[0 0 (unit/px 64)]]
               :height  (unit/px 64)
               :z-index 7}]
    [:&.Large {:flex    [[0 0 (unit/px 128)]]
               :height  (unit/px 128)
               :z-index 7}]
    [:img {:max-height    (unit/px 48)
           :border-radius (unit/percent 50)}]]])


(defn- card [theme]
  [[:.Card
    #:min {:width      (unit/rem 26)
           :height (unit/rem (golden 26))}
    {:border-radius (unit/rem 0.3)
     :overflow      :hidden}]])


(defn- container [theme]
  [[:.Flex {:flex 1}]
   #_[".Container:not(.Vertically):not(.Compact) > * + *" {:margin-left (unit/rem 1)}]
   #_[".Container.Vertically:not(.Compact) > * + *" {:margin-top (unit/rem 1)}]
   [:.Container
    #:flex {:grow   1
            :shrink 1
            :basis (unit/percent 0)}
    {:box-sizing :border-box
     :flex       1}
    [:&.Hide {:display :none}]
    ;; [(selector/& (selector/not :Vertically)) {:flex-direction :row}]
    ;; [(selector/& (selector/not :.No-wrap)) {:flex-wrap :wrap}]
    ;; [(selector/& (selector/not :.No-gap)) {:padding (unit/rem 2)}]
    ;; [(selector/& (selector/not (selector/attr-contains :class "Align"))) {:align-items :flex-start}]
    ;; [(selector/& (selector/not (selector/attr-contains :class "Justify"))) {:justify-content :flex-start}]
    [:&.Timeline {:border-bottom [[:solid (unit/px 1) (color/rgb [190 190 190])]]
                  :height        (unit/px 100)
                  :flex-grow     0}
     [:.Month {:border-right [[:solid (unit/px 1) (color/rgb [190 190 190])]]}]
     [:.Day {:border-left [[:solid (unit/px 1) (color/rgb [190 190 190])]]
             :height      (unit/px 20)}]]
    [:&.Vertically {:flex-direction :column}]
    [:&.Align-start {:align-items :flex-start}]
    [:&.Align-end {:align-items :flex-end}]
    [:&.Align-stretch {:align-items :stretch}]
    [:&.Align-center {:align-items :center}]
    [:&.Justify-space-around {:justify-content :space-around}]
    [:&.Justify-space-between {:justify-content :space-between}]
    [:&.Justify-center {:justify-content :center}]
    [:&.Justify-start {:justify-content :flex-start}]
    [:&.Justify-end {:justify-content :flex-end}]
    ;; TODO https://github.com/noprompt/garden/issueselector/127
    #_[(selector/& :.Container (selector/> (selector/not :.Compact) (selector/+ :* :*))) {:margin-left (unit/rem 2)}]
    [#{:&.Fill :.Fill} {:box-sizing :border-box
                        :flex       1
                        :min-width  0
                        :min-height 0
                        :height     (unit/percent 100)
                        :width      (unit/percent 100)}]
    [:&.Rounded {:border-radius (unit/rem 1)}]
    [:&.Raised {:box-shadow [[0 (unit/rem 0.2) (unit/rem 0.2) (color/rgba [0 0 1 0.3])]]}]]])


(defn- tmp-div [{:keys [primary]}]
  [[:.Hide {:display :none}]
   [:.Timeline {:width :auto :position :absolute}
    #_[".Button:not(.Flat) + .Button:not(.Flat)" {:border-bottom-left-radius 0
                                                :border-top-left-radius    0
                                                :position                  :relative}
     [:&:before {:content    "' '"
                 :display    :block
                 :width      (unit/rem 2.5)
                 :height     (unit/rem 3.7)
                 :position   :absolute
                 :top        (unit/rem -0.1)
                 :left       (unit/rem -2.5)
                 :background :inherit}]]]
   [:.Timeline-wrapper {:width       (unit/percent 100)
                        :height      :auto
                        :user-select :none
                        :position    :relative
                        :overflow    :hidden}]])


(defn- containers [theme]
  (map #(into '() %)
       [(tmp-div theme)
        (container theme)
        (header theme)
        (card theme)]))


(defn- typography [{:keys [font-scale font-weight]
                :or   {font-scale  (unit/em 1.8)
                       font-weight 100}}]
  [[:html #:font {:size   (unit/percent 62.5)
                  :weight font-weight
                  :family [:Roboto [:Helvetica :Neue] :Helvetica]}]
   [#{:body :input} {:font-size font-scale}]
   [#{:h1 :h2 :h3 :h4 :h5 :h6} {:font-weight :normal}]
   [:p {:line-height (unit/em 1.55) :margin-bottom (unit/em 3.1)}]
   [:.Copy {:max-width (unit/rem 65)}]
   [:.Newspaper {:text-align :justify}]
   [:.Legal {:font-size (unit/em 0.7)}
    (selector/> :*) {:margin-right (unit/rem 1)}]])


(defkeyframes pulse-color
  [:from {:background (u/gray 240)}]
  [:to {:background (u/gray 220)}])


(defkeyframes fade
  [:from {:opacity 0}]
  [:to {:opacity 1}])


(defn animations [theme]
  [pulse-color]
  [fade])


(defn- forms [{:keys [primary secondary]}]
  [[:.Auto-complete {:position      :relative
                     :width         (unit/percent 100)
                     :margin-bottom (unit/rem 1)}
    [:&.Read-only [:* {:cursor :default}]]
    [:.Textfield {:margin-bottom 0}]]
   [:.Labels
    [:.Label:first-child {:margin-left 0}]
    [:.Label:last-child {:margin-right 0}]]
   [:.Label
    [:input {:position :absolute
             :left     (unit/percent -100)
             :z-index  -10}]
    #_[(selector/+ (selector/input (selector/focus)) :label) {:opacity 1}]
    [:label {:background    (color/lighten primary 15)
             :color         (color/darken primary 40)
             :display       :inline-block
             :padding       [[(unit/rem 0.5) (unit/rem 1)]]
             :margin        [[(unit/rem 0.5) (unit/rem 1)]]
             :opacity       0.4
             :border-radius (unit/rem 0.4)
             :font-size     (unit/em 0.75)
             :position      :relative
             :z-index       1
             :user-select   :none
             :cursor        :pointer
             :margin-right  (unit/rem 0.5)}]]
   [:.Textfield {:position :relative
                 :margin   [[(unit/rem 3) 0 (unit/rem 1)]]}
    [:&.Dirty
     [:label {:left             0
              :transform        [[(translateY (unit/percent -100)) (scale 0.75)]]
              :transform-origin [[:top :left]]}]]
    [:&.Read-only [:* {:cursor :pointer}]]
    [:&.Disabled [:* {:cursor :not-allowed}]
     [:label {:color :silver}]]
    (selector/> :input) {:box-sizing :border-box
                  :margin     0
                  :display    :inline-block
                  :width      (unit/percent 100)}
    [:label {:position   :absolute
             :color      :silver
             :transition [[:all :200ms :ease]]
             :transform  (translateZ 0)
             :left       0
             :cursor     :text
             :top        (unit/rem 0.5)
             :z-index    1}]
    [:input {:background    :transparent
             :border        :none
             :border-bottom [[(unit/px 1) :solid :silver]]
             :display       :block
             :transition    [[:all :200ms :ease]]
             :font-weight   :600
             :outline       :none
             :padding       [[(unit/rem 0.5) 0]]
             :position      :relative}
     [:&:focus {:border-color primary}
      [:+ [:label {:color            :black
                   :left             0
                   :transform        [[(translateY (unit/percent -100)) (scale 0.75)]]
                   :transform-origin [[:top :left]]}]]]
     [:&:disabled {:cursor :not-allowed}]
     [:&:required
      [:&.invalid {:border-color :red}]]]
    [:.Ghost {:color    (color/rgba [0 0 1 0.3])
              :position :absolute
              :top      (unit/rem 0.5)}]]
   [:.Collection {:background         (color/rgba [254 254 255 0.85])
                  :box-shadow         [[0 (unit/rem 0.1) (unit/rem 0.2) (color/rgba [0 0 1 0.2])]]
                  :max-height         (unit/rem 30)
                  :position           :absolute
                  :width              (unit/percent 100)
                  :overflow-scrolling :touch
                  :overflow           :auto
                  :list-style         :none
                  :padding            0
                  :z-index            2}
    (selector/> :li) {:border-bottom [[:solid (unit/rem 0.1) (color/rgba [150 150 150 0.1])]]
               :padding       [[(unit/rem 1) (unit/rem 2)]]}
    [:&.Selected {:background (color/rgba [0 0 1 0.02])}]
    [:&:hover {:background-color (color/rgba [200 200 200 0.1])}]
    [:&:last-child {:border-bottom :none}]]])


(defn- calendar [{:keys [primary secondary]}]
  [[:.Date-picker {:position :relative
                   :width    (unit/percent 100)}
    [:.Calendar {:background :white
                 :z-index    3}]]
   [:.Calendar {:user-select  :none
                :text-align   :center
                :table-layout :fixed
                :width        (unit/percent 100)}
    [:th {:padding (unit/rem 2)}]
    [:td {:padding (unit/rem 1)}]
    [#{:.Previous :.Next} {:color (color/lighten secondary 50)}]
    [:.Day {:pointer :not-allowed}
     [:&.Selectable {:cursor :pointer}
      [:&:hover [:span {:background (color/lighten primary 30)}]]]
     [:span {:border-radius (unit/percent 50)
             :display       :inline-block
             :padding       (unit/rem 1)
             :height        (unit/rem 2)
             :width         (unit/rem 2)}]
     [:&.Today
      (selector/> :span) {:border [[:solid (unit/px 1) (color/darken primary 10)]]}]
     [:&.Selected.Selectable {:color  :white
                              :cursor :default}
      (selector/> :span) {:background primary}]]]])


(defn- numbers [theme]
  [[:.Worksheet {:width       (unit/percent 100)
                 :user-select :none}
    [:.Table {:border-bottom [[:solid (unit/px 1) (u/gray 230)]]
              :width         (unit/percent 100)}]
    [:.Arrow {:color         (u/gray 150)
              :padding-left  (unit/rem 1)
              :padding-right (unit/rem 1)}]
    [:.Column-headings {:background :white}
     [#{:th :td} {:border-top [[:solid (unit/px 1) (u/gray 230)]]}]]
    [:.Column-heading {:position :relative}
     [:.Dropdown-origin {:opacity    0
                         :transform  [[(translateY (unit/percent -50)) (rotateZ (unit/deg 90))]]
                         :transition [[:200ms :ease]]
                         :cursor     :pointer
                         :outline    :none
                         :border     :none
                         :background :transparent
                         :color      (u/gray 130)
                         :font-size  (unit/rem 1.5)
                         :position   :absolute
                         :right      (unit/rem 1)
                         :top        (unit/percent 50)}]
     [:&:hover
      [:.Dropdown-origin {:opacity 1}]]]
    [:tr
     [#{:td:first-child :th:first-child} {:border-left [[:solid (unit/px 1) (u/gray 230)]]}]]
    [:&.Editable
     [:.Cell {:cursor :cell}]]
    [:.Duplicate {:position :relative}
     [:&:before {:content      "' '"
                 :box-sizing   :content-box
                 :display      :block
                 :border-style :solid
                 :border-color (color/rgb [235 200 0])
                 :border-width [[0 (unit/px 1)]]
                 :box-shadow [[:inset 0 0 (unit/em 0.1) (color/rgb [245 228 90])] [0 0 (unit/em 0.1) (color/rgb [245 228 90])]]
                 :width        (unit/percent 100)
                 :height       (unit/percent 100)
                 :top          (unit/px -1)
                 :left         (unit/px -1)
                 :position     :absolute}]
     [:&.First
      [:&:before {:border-top [[:solid (unit/px 1) (color/rgb [235 200 0])]]}]]
     [:&.Last
      [:&:before {:border-bottom [[:solid (unit/px 1) (color/rgb [235 200 0])]]}]]]
    [:th [:span {:display :inline-block}]]
    [:td [:span {:display :block}]]
    [#{:td :th} [:span {:overflow      :hidden
                        :white-space   :nowrap
                        :text-overflow :ellipsis}]
     [:&.Number {:text-align :right}]
     [#{:&.Index :&.Alpha} #:font {:size   (unit/em 0.7)
                            :weight 100}]
     [:&.Smaller {:font-size (unit/em 0.45)}]
     [#{:&.Index :&.Select :&.Alpha} {:text-align :center}]]
    [:.Editable
     [:&:hover {:position :relative}
      [:&:before {:content    "' '"
                  :box-sizing :content-box
                  :display    :block
                  :border     [[:solid (unit/px 1) (u/gray 150)]]
                  :width      (unit/percent 100)
                  :height     (unit/percent 100)
                  :top        (unit/px -1)
                  :left       (unit/px -1)
                  :position   :absolute}]
      [:&:after {:content       "' '"
                 :border-radius (unit/percent 50)
                 :border        [[:solid (unit/px 1) (u/gray 150)]]
                 :display       :block
                 :position      :absolute
                 :bottom        0
                 :left          (unit/percent 50)
                 :cursor        :row-resize
                 :transform     [[(translateX (unit/percent -50)) (translateY (unit/percent 50))]]
                 :height        (unit/em 0.4)
                 :width         (unit/em 0.4)
                 :background    (color/rgb [245 228 90])}]]]
    [:.Headers {:width (unit/percent 100)}]
    [:.Body {:max-height (unit/vh 70)
             :background :white
             :width      (unit/percent 100)
             :overflow   :auto}
     [:tr:last-child [:td {:border-bottom 0}]]]
    [:table {:border-collapse :separate
             :table-layout    :fixed
             :width           :inherit}]
    [:.Titlecolumn {:background-color (u/gray 245)
                    :text-align       :left}]
    [:&.Selectable
     [:tr
      [:&:hover
       [:td {:background-color (u/gray 245)}]]
      [:&.Selected
       [#{:td :th} {:background-color (u/gray 245)}]]]]
    [#{:th :td} {:border-bottom [[:solid (unit/px 1) (u/gray 230)]]
                 :border-right  [[:solid (unit/px 1) (u/gray 230)]]
                 :padding       (unit/rem 1)}]
    [:.Auto-complete {:margin 0}
     [:span {:display :inline}]
     [:.Collection {:background :white
                    :border     [[:solid (unit/px 1) :silver]]}]
     [:.Textfield {:margin  0
                   :padding 0}
      [:input
       [:&:focus
        [:+ [:label {:opacity 0}]]]]
      [:input {:border :none}]]]]])


(defn- color-picker [theme]
  (let [swatch-size {:height (unit/em 5.625)
                     :width  (unit/em 10)}]
    [
     [:.Swatch {:width         (unit/em 4)
                :height        (unit/em 4)
                :border-radius (unit/percent 50)}
      ;; [(selector/& -webkit-color-swatch-wrapper) {:padding 0}]
      ;; [(selector/& -webkit-color-swatch) {:border 0}]
      ]
     [:.Color-picker {:background    :white
                      :display       :inline-block
                      :border-radius (unit/rem 0.25)
                      :overflow      :hidden
                      :text-align    :center}
      [:.Swatch (merge {:margin             [[0 :auto]]
                        :padding            0
                        :position           :relative
                        :-webkit-appearance :none
                        :border             0
                        :border-radius      0
                        :outline            :none
                        :overflow           :hidden} swatch-size)]
      [:.Value {:font-size (unit/em 0.7)
                :cursor    :pointer
                :padding   (unit/em 0.5)}]]]))


(defn- dropdown [{:keys [primary secondary]}]
  [[:.Dropdown {:position         :absolute
                :background       (u/gray 255)
                :border           [[:solid (unit/px 1) (u/gray 220)]]
                :box-sizing       :border-box
                :transform        [[(translateY (unit/percent 100)) (scale 1)]]
                :transform-origin [[:top :right]]
                :transition       [[:200ms (cubic-bezier 0.770, 0.000, 0.175, 1.000)]]
                :bottom           0
                :right            0
                :z-index          10
                :max-height       (unit/rem 40)
                :overflow         :auto
                :width            (unit/percent 100)
                :max-width        (unit/rem 25)}
    ;; [(selector/& (selector/not :.Open)) {:transform [[(translateY (unit/percent 100)) (scale 0)]]}]
    [:.Row {:text-align    :left
            :margin-top    (unit/em 0.5)
            :margin-bottom (unit/em 0.5)
            :width         (unit/percent 100)
            :overflow      :hidden
            :white-space   :nowrap
            :text-overflow :ellipsis}
     [:label {:display :inline-block
              :width   (unit/percent 100)}]]
    [:.Button {:border-radius  0
               :border-left    0
               :border-right   0
               :border-top     0
               :margin         0
               :text-align     :left
               :text-transform :none
               :width          (unit/percent 100)}]]])


(defn- buttons [{:keys [primary secondary positive negative]}]
  [[:.Button {:appearance     :none
              :background     (u/gray 230)
              :border-radius  (unit/em 0.2)
              :border         [[:solid (unit/em 0.1) (u/gray 215)]]
              :outline        :none
              :user-select    :none
              :min-width      (unit/rem 8)
              :text-transform :uppercase
              :transition     [[:background-color :200ms :ease]]
              :padding        [[(unit/em 1) (unit/em 2)]]}
    [:&:hover {:background-color (u/gray 240)}]
    [:&.Secondary {:background-color secondary
                   :border-color     secondary
                   :color            (if (u/dark? secondary) :white :black)
                   }
     [:&:hover {:background-color (color/lighten secondary 10)}]]
    [:&.Primary {:background-color primary
                 :border-color     primary
                 :color            (if (u/dark? primary) :white :black)
                 }
     [:&:hover {:background-color (color/lighten primary 10)}]]
    [:&.Icon {:padding [[(unit/em 0.7) (unit/em 2)]]}
     [:i {:font-size (unit/em 1.5)}]]
    [:&.No-chrome {:border-color     :transparent
                   :background-color :transparent}]
    ;; [(selector/& (selector/not :disabled)) {:cursor :pointer}]
    [:&.Positive {:background-color positive
                  :border-color     positive}]
    [:&.Negative {:background-color negative
                  :border-color     negative}]
    [:&.Flat {:background-color :transparent
              :border           [[:solid (unit/em 0.1) :inherit]]}]
    [:.Rounded {:border-radius (unit/em 2)}]]])


(defn- tmp [{:keys [primary secondary background positive negative]}]
  [[:.Dialog {:position :fixed
              :left     0
              :top      0
              :height   (unit/percent 100)
              :width    (unit/percent 100)
              :margin   [[0 :!important]]
              :z-index  10}
    ;; [(selector/& (selector/not :.Open)) {:display :none}]
    [:&.Open
     [:.Backdrop {:opacity 1
                  :animation [[fade :200ms :ease]]
                  :z-index 10}]]]
   [:.Dialog-content {:background    :white
                      :border-radius (unit/rem 0.8)
                      :position      :absolute
                      :left          (unit/percent 50)
                      :top           (unit/percent 50)
                      :transform     [[(translateY (unit/percent -50)) (translateX (unit/percent -50))]]
                      :z-index       12}]
   [:body {:background-color background}]
   [:menu [:a {:display :block}]]
   [:a {:color           secondary ; primary
        :text-decoration :none}
    [:&.Primary {:color primary}]
    [:&:hover {:color (color/darken secondary 30)}
     [:&.Primary {:color primary}]]]])


(def docs
  (let [contrast (u/gray 240)
        triangle (linear-gradient (unit/deg 45)
                                  [contrast (unit/percent 25)]
                                  [:transparent (unit/percent 25)]
                                  [:transparent (unit/percent 75)]
                                  [contrast (unit/percent 75)]
                                  contrast)]
    [:.Container
     [:&.Demo {:background          (repeat 2 triangle)
               :background-position [[0 0] [(unit/px 10) (unit/px 10)]]
               :background-size     [[(unit/px 20) (unit/px 20)]]
               :min-width           (unit/vw 50)
               :min-height          (unit/vh 35)
               :box-shadow          [[:inset 0 (unit/px 2) (unit/px 8) (color/rgba [0 0 1 0.3])]]}
      [:&.Horizontally
       [:&.Align-left {:align-items :flex-start}]
       [:&.Align-right {:justify-content :flex-end}]]
      [:&.Vertically
       [:&.Align-top {:align-items :flex-start}]
       [:&.Align-bottom {:justify-content :flex-end}]]
      [:.Fill {:background (-> theme :default :primary)
               :border     [[:dashed (unit/px 1) (color/rgba [0 0 1 0.2])]]}]]]
    [#{:.Code :pre} {:background    (u/gray 250)
                    :border-radius (unit/rem 0.3)
                    :color         (u/gray 170)
                    :font-family   [[:monospace]]
                    :white-space   :pre
                    :padding       (unit/rem 2)}]
    [#{:.Code :code} {:line-height 1.8}
     [:label {:background    (color/rgb [38 189 230])
              :color         :white
              :border-radius (unit/rem 1)
              :padding       [[(unit/rem 0.2) (unit/rem 0.5)]]}]]
    [:.Keyword {:color (color/rgb [60 140 180])}]
    [:.Symbol {:color (color/rgb [60 220 190])}]
    [:.Parens {:color (color/rgb [180 70 200])}]
    [:.Demo-box {:border           [[:solid (unit/px 1) :silver]]
                 :background-color :white
                 :padding          (unit/rem 2)}]
    [:body {:background :white}]
    [:.Functional-hide {:position :absolute
                        :left     (unit/vw -200)}]
    [:.Sidebar {:background (u/gray 245)}]))


(def screen
  (let [theme (:default theme)]
    (map #(into '() %) [(structure theme)
                        (layouts theme)
                        (containers theme)
                        (numbers theme)
                        (forms theme)
                        (color-picker theme)
                        (calendar theme)
                        (typography theme)
                        (buttons theme)
                        (dropdown theme)
                        (checkbox/style theme)
                        (clamp/style theme)
                        (boundary/style theme)
                        (progress-bar/style theme)])))
