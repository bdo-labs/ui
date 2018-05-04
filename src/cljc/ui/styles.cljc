(ns ui.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes]])
            [garden.stylesheet :refer [calc at-media]]
            [garden.units :as unit]
            [garden.media :as media]
            [garden.color :as color]
            [garden.selectors :as selector]
            [ui.util :as util]
            [ui.element.badge.styles :as badge]
            [ui.element.chooser.styles :as chooser]
            [ui.element.textfield.styles :as textfield]
            [ui.element.searchfield.styles :as searchfield]
            [ui.element.collection.styles :as collection]
            [ui.element.calendar.styles :as calendar]
            [ui.element.checkbox.styles :as checkbox]
            [ui.element.containers.styles :as containers]
            [ui.element.sidebar.styles :as sidebar]
            [ui.element.menu.styles :as menu]
            [ui.element.button.styles :as button]
            [ui.element.icon.styles :as icon]
            [ui.element.progress-bar.styles :as progress-bar]
            [ui.element.modal.styles :as modal]))

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
;;   "container".

(defcssfn linear-gradient)
(defcssfn rotateZ)
(defcssfn scale)
(defcssfn translateX)
(defcssfn translateY)
(defcssfn translateZ)

(def theme
  {:default {:background (color/rgb [245 245 245])
             :primary (color/rgb [70 111 226])
             :secondary (color/rgb [247 90 109])
             :tertiary (color/rgb [101 124 145])
             :positive (color/rgb [34 192 100])
             :negative (color/rgb [232 83 73])
             :font-weight 100
             :font-base (unit/rem 1.6)
             :font-scale :augmented-fourth}})

(def breakpoint
  {:phone {:max-width (unit/px 599)}
   :pad-portrait {:min-width (unit/px 600)}
   :pad-landscape {:min-width (unit/px 900)}
   :desktop {:min-width (unit/px 1200)}
   :big-screen {:min-width (unit/px 1800)}})

(def scales
  {:minor-second     (/ 16 15)
   :major-second     (/ 9 8)
   :minor-third      (/ 6 5)
   :major-third      (/ 5 4)
   :perfect-fourth   (/ 4 3)
   :augmented-fourth (/ 1.411 1)
   :perfect-fifth    (/ 3 2)
   :golden           (/ 1.61803 1)})

(defn- base [{:keys [primary secondary tertiary positive negative]
              :as   theme}]
  [[#{:b :stron} {:font-weight :bolder}]
   [:img {:border-style :none}]
   [:ul {:list-style-position :inside}]
   [:.face-primary {:color primary}]
   [:.face-secondary {:color secondary}]
   [:.face-tertiary {:color tertiary}]])

(defn- structure [theme]
  [[:body {:overflow :hidden}]
   [:.hide {:display :none}]
   [#{:html :body :#app :#app:first-child} {:height (unit/percent 100)
                                            :width  (unit/percent 100)}]
   [#{:html :body :menu :ul :input :.Chooser} {:margin  0
                                               :padding 0}]
   [:main {:height (calc (- (unit/vh 100) (unit/px 64)))}]
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
              :margin-bottom (unit/rem 4)
              :max-width (unit/rem 100)
              :text-align :left}
    [:p {:width (unit/rem 60)}]
    [:pre {:width (unit/rem 70)
           :overflow :auto}]]])

(defn- layouts
  [{:keys [background]}]
  [[:.Backdrop {:background (color/rgba [0 0 0 0.5])
                :width      (unit/percent 100)
                :height     (unit/percent 100)
                :position   :absolute
                :left       0
                :top        0
                :transition [[:opacity :500ms :ease]]
                :opacity    0
                :z-index    -1}]])

(defn- header [{:keys [background]}]
  [[#{:.Header :.Card} {:background-color background}]
   [:.Header {:z-index         10
              :box-shadow [[(unit/rem 0) (unit/rem 0.1) (unit/rem 0.8) (color/rgba [0 0 0 0.2])]]
              :width           (unit/vw 100)
              :box-sizing      :border-box
              :padding         [[0 (unit/rem 2)]]
              :position        :relative}
    [:&.small {:flex   [[0 0 (unit/px 64)]]
               :height (unit/px 64)}]
    [:&.large {:flex   [[0 0 (unit/px 128)]]
               :height (unit/px 128)}]]])

(defn- card [{:keys [font-scale]
              :as   theme}]
  [[:.Card
    {:min-width (unit/rem 26)
     :max-width (unit/rem 70)
     :width     (unit/percent 100)
     :position :relative}]])

(defn- containers [theme]
  (map #(into '() %)
       [(containers/style theme)
        (header theme)
        (card theme)]))

(defn- typography [{:keys [font-base font-weight font-scale]
                    :or   {font-base   (unit/em 1.8)
                           font-weight 100}}]
  [[:html {:font-size   (unit/percent 62.5)
           :font-weight font-weight
           :font-family [:-apple-system :BlinkMacSystemFont [:Segoe :UI] :Roboto :Oxygen-Sans :Ubuntu :Cantarell [:Helvetica :Neue] :Helvetica :Arial :sans-serif]}]
   [#{:body} {:line-height 1.45}]
   [#{:body :input} {:font-size font-base}]
   [#{:h1 :h2 :h3 :h4 :h5 :h6} {:font-weight :normal
                                :line-height 1.2}]
   (for [n (range 1 6)]
     (let [size  (* (Math/abs (- n 6)) 0.3)
           scale (get scales font-scale)
           font-declaration {:font-size (unit/em (* size scale))}]
       [(set [(keyword (str "h" n)) (keyword (str ".h" n))]) font-declaration]))
   [:p {:margin-bottom (unit/em 1.3)}]
   [:.Copy {:max-width (unit/rem 65)}]
   [:.Newspaper {:text-align :justify}]
   #_[:.Legal {:font-size (unit/em 0.7)}
      (selector/> :*) {:margin-right (unit/rem 1)}]])

(defkeyframes pulse-color
  [:from {:background (util/gray 240)}]
  [:to {:background (util/gray 220)}])

(defkeyframes fade-up
  [:from {:opacity 0
          :transform (translateY (unit/percent 25))}]
  [:to {:opacity 1
        :transform (translateY 0)}])

(defkeyframes fade
  [:from {:opacity 0}]
  [:to {:opacity 1}])

(defkeyframes up
  [:from {:transform (translateY (unit/percent 50))}]
  [:to {:transform (translateY 0)}])

(defkeyframes scaled
  [:from {:transform (scale 0)}]
  [:to {:transform (scale 1)}])

(defkeyframes can-edit
  [0 {:background (util/gray 200)}]
  [100 {:background (util/gray 240)}])

(defn- animations [theme]
  (map #(into '() %) [[pulse-color]
                      [fade]
                      [up]
                      [fade-up]
                      [scaled]]))

(defn- forms [{:keys [primary secondary]}]
  [[:.disabled {:cursor :not-allowed
                :pointer-events :none
                :opacity 0.3}]
   [:.read-only {:cursor :default}]
   [:.Labels
    [:.Label:first-child {:margin-left 0}]
    [:.Label:last-child {:margin-right 0}]]
   [:.Label
    [:input {:position :absolute
             :left     (unit/percent -100)
             :z-index  -10}]
    [:label {:background    (color/lighten secondary 15)
             :color         (color/darken secondary 40)
             :display       :inline-block
             :padding       [[(unit/rem 0.5) (unit/rem 1)]]
             :margin        [[(unit/rem 0.5) (unit/rem 1)]]
             :opacity       0.4
             :border-radius (unit/rem 0.4)
             :font-size     (unit/em 0.75)
             :position      :relative
             :transition    [:all :500ms :ease]
             :z-index       1
             :user-select   :none
             :cursor        :pointer
             :margin-right  (unit/rem 0.5)}]]])

(defn- numbers [{:keys [primary secondary tertiary]}]
  [[:.Worksheet {:width       (unit/percent 100)
                 :user-select :none}
    [:.Row
     [#{:input :label} {:max-width     (unit/percent 100)
                        :text-overflow :ellipsis
                        :white-space   :nowrap
                        :overflow      :hidden}]]
    [:.Table {:border-bottom [[:solid (unit/px 1) (util/gray 230)]]
              :width         (unit/percent 100)}]
    [:.Arrow {:color         (util/gray 150)
              :font-size     (unit/em 0.7)
              :padding-left  (unit/rem 1)
              :padding-right (unit/rem 1)}]
    [:.Column-headings {:background :white}
     [#{:th :td} {:border-top [[:solid (unit/px 1) (util/gray 230)]]}]]
    [#{:th} {:position :relative}
     [:.Dropdown {:background :white
                  :position   [[:absolute :!important]]
                  :right      0}
      [:.Button {:border :transparent}]]
     [:.Dropdown-origin {:opacity    0
                         :transform  [[(translateY (unit/percent -50)) (rotateZ (unit/deg 90))]]
                         :transition [[:200ms :ease]]
                         :cursor     :pointer
                         :outline    :none
                         :border     :none
                         :background :transparent
                         :color      (util/gray 130)
                         :font-size  (unit/rem 1.5)
                         :position   :absolute
                         :right      (unit/rem 1)
                         :top        (unit/percent 50)}]
     [:&:hover
      [:.Dropdown-origin {:opacity 1}]]]
    [:tr
     [#{:td:first-child :th:first-child} {:border-left [[:solid (unit/px 1) (util/gray 230)]]}]]
    [:.duplicate {:position   :relative
                  :background :yellow}
     [:&:before {:content      "' '"
                 :box-sizing   :content-box
                 :display      :block
                 :border-style :solid
                 :border-color (color/rgb [235 200 0])
                 :border-width [[0 (unit/px 1)]]
                 :box-shadow   [[:inset 0 0 (unit/em 0.1) (color/rgb [245 228 90])] [0 0 (unit/em 0.1) (color/rgb [245 228 90])]]
                 :width        (unit/percent 100)
                 :height       (unit/percent 100)
                 :top          (unit/px -1)
                 :left         (unit/px -1)
                 :position     :absolute}]
     [:&.first
      [:&:before {:border-top [[:solid (unit/px 1) (color/rgb [235 200 0])]]}]]
     [:&.last
      [:&:before {:border-bottom [[:solid (unit/px 1) (color/rgb [235 200 0])]]}]]]
                                        ; FIXME Replace with :not selector ones stable
    [#{:.locked} {:cursor [[:default :!important]]
                  :color  [[(color/rgb (doall (vec (repeat 3 120)))) :!important]]}
     [:&:after {:display [[:none :!important]]}]]
    [:th [:span {:display :inline-block}]]
    [:td [:span {:display :block}]]
    [#{:td :th} [:span {:overflow      :hidden
                        :white-space   :nowrap
                        :text-overflow :ellipsis}]
     [#{:&.number :&.inst} {:text-align :right}]
     [#{:&.numeric :&.Alpha} {:font-size   (unit/em 0.7)
                              :font-weight 100}]
     [:&.smaller {:font-size (unit/em 0.45)}]
     [#{:&.numeric :&.select :&.alpha} {:text-align :center}]]
    [:.not-editable {:background (util/gray 250)}]
    [:.can-edit {:background (util/gray 250)}]
    [:.editable
     [:&.cell {:cursor :cell}]
     [:&:hover {:position :relative}
      [:&:before {:content        "' '"
                  :box-sizing     :content-box
                  :display        :block
                  :border         [[:solid (unit/px 1) (util/gray 185)]]
                  :width          (unit/percent 100)
                  :height         (unit/percent 100)
                  :top            (unit/px -1)
                  :left           (unit/px -1)
                  :pointer-events :none
                  :position       :absolute}]
      #_[:&:after {:content       "' '"
                   :border-radius (unit/percent 50)
                   :border        [[:solid (unit/px 1) (util/gray 150)]]
                   :display       :block
                   :position      :absolute
                   :bottom        0
                   :left          (unit/percent 50)
                   :cursor        :row-resize
                   :transform     [[(translateX (unit/percent -50)) (translateY (unit/percent 50))]]
                   :height        (unit/em 0.4)
                   :width         (unit/em 0.4)
                   :background    (color/rgb [245 228 90])}]]]
    [:.Table-Header {:width (unit/percent 100)}]
    [:.Table-Body {:max-height (unit/vh 70)
                   :background :white
                   :width      (unit/percent 100)
                   :overflow   :auto}
     [:tr:last-child [:td {:border-bottom 0}]]]
    [:.Body-cell [:span {:animation [[:200ms :fade :ease-in-out]]}]
     [:&.map {:padding 0}
      [:.Textfield {:width (calc (- (unit/percent 100) (unit/rem 1)))
                    :margin-top (unit/em 0.5)
                    :margin-left (unit/em 0.5)}]
      [:.Dropdown {:margin-top (unit/em 1)}]]]
    [:.Has-chooser {:line-height (unit/em 3)
                    :padding [[0 (unit/em 0.5)]]
                    :min-height (calc (- (unit/percent 100) (unit/rem 1)))}]
    [:table {:border-collapse :separate
             :table-layout    :fixed
             :width           :inherit}]
    [:.Title-column {:text-align :left}
     [:span {:font-weight 600}]]
    [:&.selectable
     [:tr
      [:&:hover
       [:td {:background-color (util/gray 245)}]]
      [:&.selected
       [#{:td :th} {:background-color (util/gray 245)}]]]]
    [#{:th :td} {:border-bottom [[:solid (unit/px 1) (util/gray 230)]]
                 :border-right  [[:solid (unit/px 1) (util/gray 230)]]
                 :padding       (unit/rem 1)}]
    [:.Chooser
     [:span {:display :inline}]
     [:.Textfield {:margin  0
                   :padding 0}
      [#{:&.placeholder :&.not-empty}
       [:label {:display :none}]]
      [:input {:border     :none
               :margin-top (unit/rem -1) ;; TODO Why is this necessary for mid-alignment
               :padding    0}
       [:&:focus
        [:+ [:label {:opacity 0}]]]]]]]])

(defn- color-picker [theme]
  (let [swatch-size {:height (unit/em 5.625)
                     :width  (unit/em 10)}]
    [[:.Swatch {:width         (unit/em 4)
                :height        (unit/em 4)
                :border-radius (unit/percent 50)}]
     [:.Color-wheel {:background (linear-gradient (unit/deg 180) :red :yellow :lime :aqua :blue :magenta)
                     :height     (unit/em 1)
                     :width      (-> swatch-size :width)
                     :position :absolute
                     :z-index 300}
      [:&:after {:background (linear-gradient (unit/deg 90) (color/rgba [255 255 255 1]) (color/rgba [0 0 0 0]) (color/rgba [0 0 0 1]))
                 :content "' '"
                 :display :block
                 :position :absolute
                 :left 0
                 :right 0
                 :top 0
                 :bottom 0}]]
     [:.Color-picker {:background    :white
                      :display       :inline-block
                      :border-radius (unit/rem 0.25)
                      :overflow      :hidden
                      :text-align    :center}
      [:&.raised {:box-shadow [[(unit/rem 0) (unit/rem 0.1) (unit/rem 0.8) (color/rgba [0 0 0 0.2])]]}]
      [:.Swatch swatch-size
       [#{:span :input} swatch-size]
       [:input {:cursor             :pointer
                :overflow           :hidden
                :position           :relative
                :outline            :none
                :margin             [[0 :auto]]
                :border             0
                :border-radius      0
                :-webkit-appearance :none
                :padding            0}]]
      [:.Value {:font-size (unit/em 0.7)
                :cursor    :pointer
                :padding   (unit/em 0.5)}]]]))

(defn- in-doc [{:keys [primary secondary]}]
  (let [contrast (util/gray 240)
        triangle [(linear-gradient (unit/deg 45)
                                   [contrast (unit/percent 25)]
                                   [:transparent (unit/percent 25)]
                                   [:transparent (unit/percent 75)]
                                   [contrast (unit/percent 75)]
                                   contrast)]]
    [[:.Sidebar
      [:sidebar {:box-shadow :none
                 :border-right [[(unit/px 1) :solid (util/gray 230)]]}]]
     [:.Container
      [:&.demo {:background          (vec (repeat 2 triangle))
                :background-position [[0 0] [(unit/px 10) (unit/px 10)]]
                :background-size     [[(unit/px 20) (unit/px 20)]]
                :box-shadow          [[:inset 0 (unit/px 2) (unit/px 8) (color/rgba [0 0 0 0.3])]]
                :width (unit/percent 100)
                :min-width           (unit/em 21)
                :min-height          (unit/em 10)}]
      [:&.fill-demo
       [:.fill {:background (-> theme :default :secondary)
                :border     [[:dashed (unit/em 0.2) (color/rgba [0 0 0 0.2])]]}]]]
     [:pre {:padding (unit/rem 2)}]
     [#{:.Code :pre :.hljs} {:background    (color/rgb [34 38 68])
                             :border-radius (unit/rem 0.3)
                             :font-family   "roboto mono"
                             :font-size     (unit/em 0.9)
                             :color         (color/rgb [60 70 100])
                             :white-space   :pre}
      [:span {:font-family "roboto mono"}]]
     [#{:.Code :code} {:line-height 1.8}
      [:label {:background    (color/rgb [38 189 230])
               :color         :white
               :border-radius (unit/rem 1)
               :padding       [[(unit/rem 0.2) (unit/rem 0.5)]]}]]
     [:.Keyword {:color     (color/rgb [60 140 180])
                 :min-width (unit/em 4)}]
     [:.Symbol {:color (color/rgb [60 220 190])}]
     [:.Parens {:color (color/rgb [180 190 100])}]
     [:.Demo-box {:border           [[:solid (unit/px 1) :silver]]
                  :background-color :white
                  :padding          (unit/rem 2)}]
     [:body {:background (color/rgb [65 88 208])}]
     [:.Functional-hide {:position :absolute
                         :left     (unit/vw -200)}]]))

(def docs
  (let [theme (:default theme)]
    (map #(into '() %) [(in-doc theme)])))

(defn custom [theme]
  (map #(into '() %) [(animations theme)
                      (base theme)
                      (structure theme)
                      (layouts theme)
                      (containers theme)
                      (numbers theme)
                      (forms theme)
                      (color-picker theme)
                      (calendar/style theme)
                      (typography theme)
                      (chooser/style theme)
                      (collection/style theme)
                      (sidebar/style theme)
                      (badge/style theme)
                      (textfield/style theme)
                      (searchfield/style theme)
                      (button/style theme)
                      (menu/style theme)
                      (checkbox/style theme)
                      (modal/style theme)
                      (icon/style theme)
                      (progress-bar/style theme)]))

(def screen
  (let [theme (:default theme)]
    (custom theme)))

(defn inject [document id style]
  #?(:clj (println "Injecting styles only work interactively")
     :cljs (let [new-styles-el (.createElement document "style")]
             (.setAttribute new-styles-el "id" id)
             (.setAttribute new-styles-el "type" "text/css")
             (-> new-styles-el (.-innerHTML) (set! style))
             (if-let [styles-el (.getElementById document id)]
               (-> styles-el (.-parentNode) (.replaceChild new-styles-el styles-el))
               (do (.appendChild (.-head document) new-styles-el)
                   new-styles-el)))))
