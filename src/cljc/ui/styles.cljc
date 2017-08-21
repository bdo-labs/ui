(ns ui.styles
  #?(:cljs (:require-macros [garden.def :refer [defcssfn defkeyframes]]))
  (:require #?(:clj [garden.def :refer [defcssfn defkeyframes]])
            [garden.stylesheet :as stylesheet]
            [garden.units :as unit]
            [garden.color :as color]
            [garden.selectors :as selector :refer [defpseudoelement]]
            [ui.util :as u]
            [ui.element.boundary :as boundary]
            [ui.element.checkbox :as checkbox]
            [ui.element.containers :as containers]
            [ui.element.menu :as menu]
            [ui.element.button :as button]
            [ui.element.clamp :as clamp]
            [ui.element.progress-bar :as progress-bar]
            [ui.element.modal :as modal]
            [ui.element.loaders :as loaders]))

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
             :primary (color/rgb [46 175 164])
             :secondary (color/rgb [12 164 226])
             :tertiary (color/rgb [101 124 145])
             :positive (color/rgb [34 192 100])
             :negative (color/rgb [232 83 73])
             :font-weight 100
             :font-base (unit/rem 1.8)
             :font-scale :augmented-fourth}})

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
              :as theme}]
  [[:ul {:list-style-position :inside}]
   [:.face-primary {:color primary}]
   [:.face-secondary {:color secondary}]
   [:.face-tertiary {:color tertiary}]])

(defn- structure [theme]
  [[:body {:overflow :hidden}]
   [:.hide {:display :none}]
   ;; TODO #app:first-child, really!?
   [#{:html :body :#app :#app:first-child} {:height (unit/percent 100)
                                            :width  (unit/percent 100)}]
   [#{:html :body :menu :ul} {:margin  0
                              :padding 0}]
   [:main {:height (stylesheet/calc (- (unit/vh 100) (unit/px 64)))}]
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

(defn- layouts
  [{:keys [background]}]
  [[:.Backdrop {:background (color/rgba [0 0 1 0.5])
                :width      (unit/percent 100)
                :height     (unit/percent 100)
                :position   :absolute
                :left       0
                :top        0
                :transition [[:opacity :500ms :ease]]
                :opacity    0
                :z-index    -1}]
   ;; Should use immediate descender-selector
   ;; [[:.Sidebar.Locked.Align-left :.Slider :main] {:margin-left (unit/px 360)}]
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
               :line-height 2}]
    [:main {:width              (unit/percent 100)
            :overflow           :auto
            :overflow-scrolling :touch
            :position           :relative
            :height             (unit/percent 100)}]
    #_[:&.Locked
       [:main {:width (stylesheet/calc (- (unit/percent 100) (unit/px 360)))}]]
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
  [[#{:.Header :.Card} {:background-color :white}]
   [:.Header {:justify-content :space-between
              :align-items     :center
              :width           (unit/vw 100)
              :box-sizing      :border-box
              :padding         [[0 (unit/rem 1)]]
              :position        :relative}
    [:&.small {:flex    [[0 0 (unit/px 64)]]
               :height  (unit/px 64)
               :z-index 7}]
    [:&.large {:flex    [[0 0 (unit/px 128)]]
               :height  (unit/px 128)
               :z-index 7}]]])


(defn- card [{:keys [font-scale]
              :as theme}]
  [[:.Card
    {:min-width  (unit/rem 26)
     :min-height (unit/rem (* (get scales font-scale) 26))}
    {:border-radius (unit/rem 0.3)
     :overflow      :hidden}]])

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
           :font-family [:Roboto [:Helvetica :Neue] :Helvetica]}]
   [#{:body} {:line-height 1.45}]
   [#{:body :input} {:font-size font-base}]
   [#{:h1 :h2 :h3 :h4 :h5 :h6} {:font-weight :normal
                                :line-height 1.2}]
   (for [n (range 1 6)]
     (let [size  (* (Math/abs (- n 6)) 0.3)
           scale (get scales font-scale)]
       [(keyword (str "h" n)) {:font-size (unit/em (* size scale))}]))
   [:p {:margin-bottom (unit/em 1.3)}]
   [:.Copy {:max-width (unit/rem 65)}]
   [:.Newspaper {:text-align :justify}]
   [:.Legal {:font-size (unit/em 0.7)}
    (selector/> :*) {:margin-right (unit/rem 1)}]])

(defkeyframes pulse-color
  [:from {:background (u/gray 240)}]
  [:to {:background (u/gray 220)}])

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

(defn- animations [theme]
  [pulse-color]
  [fade]
  [up]
  [fade-up])

(defn- forms [{:keys [primary secondary]}]
  [[:.Auto-complete {:position      :relative
                     :width         (unit/percent 100)
                     :margin-bottom (unit/rem 1)}
    [:&.read-only [:* {:cursor :default}]]
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
   [:.Textfield {:position :relative}
    [:&.Placeholder {:margin   [[(unit/rem 3) 0 (unit/rem 1)]]}]
    [:&.dirty
     [:label {:left             0
              :transform        [[(translateY (unit/percent -100)) (scale 0.75)]]
              :transform-origin [[:top :left]]}]]
    [:&.read-only [:* {:cursor :pointer}]]
    [:&.disabled [:* {:cursor :not-allowed}]
     [:label {:color :silver}]]
    [:input {:box-sizing :border-box
             :margin     0
             :display    :inline-block
             :width      (unit/percent 100)}]
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
    ;; TODO Make it an immediate child selector
    [:li {:border-bottom [[:solid (unit/rem 0.1) (color/rgba [150 150 150 0.1])]]
          :padding       [[(unit/rem 1) (unit/rem 2)]]}
     [:&.Selected {:background (color/rgba [0 0 1 0.02])}]
     [:&:hover {:background-color (color/rgba [200 200 200 0.1])}]
     [:&:last-child {:border-bottom :none}]]]])

(defn- calendar [{:keys [primary secondary]}]
  [[:.Date-picker {:position :relative
                   :width    (unit/percent 100)}
    [:.Calendar {:background :white
                 :z-index    3}]]
   [:.Calendar
    [:table {:user-select  :none
             :text-align   :center
             :table-layout :fixed
             :width        (unit/percent 100)}]
    [:th {:padding (unit/rem 2)}]
    [:td {:padding (unit/rem 1)}]
    [#{:.Previous :.Next} {:color (color/lighten secondary 50)}]
    [:.Day {:pointer :not-allowed}
     [:&.selectable {:cursor :pointer}
      [:&:hover [:span {:background (color/lighten primary 30)}]]]
     [:span {:border-radius (unit/percent 50)
             :display       :inline-block
             :padding       (unit/rem 1)
             :height        (unit/rem 2)
             :width         (unit/rem 2)}]
     [:&.today
      (selector/> :span) {:border [[:solid (unit/px 1) (color/darken primary 10)]]}]
     [:&.selected.selectable {:color  :white
                              :cursor :default}
      (selector/> :span) {:background primary}]]]])

(defn- numbers [theme]
  [[:.Worksheet {:width       (unit/percent 100)
                 :user-select :none}
    [:.Table {:border-bottom [[:solid (unit/px 1) (u/gray 230)]]
              :width         (unit/percent 100)}]
    [:.Arrow {:color         (u/gray 150)
              :font-size     (unit/em 0.7)
              :padding-left  (unit/rem 1)
              :padding-right (unit/rem 1)}]
    [:.Column-headings {:background :white}
     [#{:th :td} {:border-top [[:solid (unit/px 1) (u/gray 230)]]}]]
    [#{:th} {:position :relative}
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
    [:.Duplicate {:position   :relative
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
     [:&.First
      [:&:before {:border-top [[:solid (unit/px 1) (color/rgb [235 200 0])]]}]]
     [:&.Last
      [:&:before {:border-bottom [[:solid (unit/px 1) (color/rgb [235 200 0])]]}]]]
                                        ; FIXME Replace with :not selector ones stable
    [#{:.Locked} {:cursor [[:default :!important]]
                  :color  [[(color/rgb (doall (vec (repeat 3 120)))) :!important]]}
     [:&:after {:display [[:none :!important]]}]]
    [:th [:span {:display :inline-block}]]
    [:td [:span {:display :block}]]
    [#{:td :th} [:span {:overflow      :hidden
                        :white-space   :nowrap
                        :text-overflow :ellipsis}]
     [:&.number {:text-align :right}]
     [#{:&.index :&.Alpha} {:font-size   (unit/em 0.7)
                            :font-weight 100}]
     [:&.smaller {:font-size (unit/em 0.45)}]
     [#{:&.index :&.select :&.alpha} {:text-align :center}]]
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
      #_[:&:after {:content       "' '"
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
      [:&.selected
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
      [:&.dirty
       [:label {:display :none}]]
      [:input
       [:&:focus
        [:+ [:label {:opacity 0}]]]]
      [:input {:border :none}]]]]])

(defn- color-picker [theme]
  (let [swatch-size {:height (unit/em 5.625)
                     :width  (unit/em 10)}]
    [[:.Swatch {:width         (unit/em 4)
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

(defn- in-doc [{:keys [primary]}]
  (let [contrast (u/gray 240)
        triangle [(linear-gradient (unit/deg 45)
                                   [contrast (unit/percent 25)]
                                   [:transparent (unit/percent 25)]
                                   [:transparent (unit/percent 75)]
                                   [contrast (unit/percent 75)]
                                   contrast)]]
    [[:.Marketing {:background (linear-gradient (unit/deg 45) (color/rgb [64 60 82]) (color/rgb [34 25 50]))
                   :color (color/rgb [240 240 240])}
      [:.raised {:color (color/rgb [50 50 50])}]
      [:section {:margin-top (unit/rem 5)
                 :margin-bottom (unit/rem 5)}]]
     [:.Container
      [:&.demo {:background          (vec (repeat 2 triangle))
                :background-position [[0 0] [(unit/px 10) (unit/px 10)]]
                :background-size     [[(unit/px 20) (unit/px 20)]]
                :min-width           (unit/vw 50)
                :min-height          (unit/vh 35)
                :box-shadow          [[:inset 0 (unit/px 2) (unit/px 8) (color/rgba [0 0 1 0.3])]]}
       [:.fill {:background (-> theme :default :primary)
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
                         :left     (unit/vw -200)}]]))

(def docs
  (let [theme (:default theme)]
    (map #(into '() %) [(in-doc theme)])))

(def screen
  (let [theme (:default theme)]
    (map #(into '() %) [(animations theme)
                        (base theme)
                        (structure theme)
                        (layouts theme)
                        (containers theme)
                        (numbers theme)
                        (forms theme)
                        (color-picker theme)
                        (calendar theme)
                        (typography theme)
                        (button/style theme)
                        (menu/style theme)
                        (checkbox/style theme)
                        (modal/style theme)
                        (clamp/style theme)
                        (boundary/style theme)
                        (progress-bar/style theme)
                        (loaders/style theme)])))
